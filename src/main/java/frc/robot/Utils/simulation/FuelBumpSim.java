// Applies bump physics to fuel game pieces on the 2026 REBUILT field.
// Same frictionless-slide model as RobotBumpSim, but simplified for spherical game pieces.

package frc.robot.Utils.simulation;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import org.dyn4j.geometry.Vector2;
import org.ironmaple.simulation.SimulatedArena;
import org.ironmaple.simulation.gamepieces.GamePiece;
import org.ironmaple.simulation.gamepieces.GamePieceOnFieldSimulation;
import org.ironmaple.utils.mathutils.GeometryConvertor;

public class FuelBumpSim {

    private static final double PERIOD = 0.02;
    private static final double GRAVITY_Z = -9.81;

    /** Fuel ball radius (7.5 cm), used as collision distance from ramp surface. */
    private static final double FUEL_RADIUS = 0.075;

    /** Coefficient of restitution for fuel-bump Z collisions. */
    private static final double FUEL_COR = 0.15;

    private static final double SEGMENT_TOLERANCE = 1e-6;

    private final IdentityHashMap<GamePieceOnFieldSimulation, FuelState> states = new IdentityHashMap<>();

    private static class FuelState {
        double zPos = 0;
        double zVel = 0;
        boolean onRamp = false;
        double simXPos = 0;
        double simXVel = 0;
    }

    /**
     * Advances bump physics for all fuel pieces and returns corrected 3D poses.
     *
     * <p>Call from {@code simulationPeriodic()} and use the returned array for logging
     * instead of {@code SimulatedArena.getGamePiecesArrayByType("Fuel")}.
     *
     * @param subticks physics sub-steps per period (5 recommended for 20 ms loops)
     * @return Pose3d array of all fuel pieces with corrected Z heights
     */
    public Pose3d[] update(int subticks) {
        SimulatedArena arena = SimulatedArena.getInstance();
        Set<GamePieceOnFieldSimulation> groundedPieces = arena.gamePiecesOnField();
        double dt = PERIOD / subticks;

        // Clean up state for pieces no longer on field
        states.keySet().retainAll(groundedPieces);

        List<Pose3d> allFuelPoses = new ArrayList<>();

        // Process grounded fuel pieces with bump physics
        for (GamePieceOnFieldSimulation piece : groundedPieces) {
            if (!"Fuel".equals(piece.type)) continue;

            FuelState state = states.computeIfAbsent(piece, k -> new FuelState());
            Pose2d pose = piece.getPoseOnField();
            Vector2 vel = piece.getLinearVelocity();
            double vx = vel.x;

            for (int tick = 0; tick < subticks; tick++) {
                double currentX = state.onRamp ? state.simXPos : pose.getX();
                double currentVx = state.onRamp ? state.simXVel : vx;

                // Z gravity
                state.zVel += GRAVITY_Z * dt;
                state.zPos += state.zVel * dt;

                // Check all bump segments
                double gravAccelXSum = 0;
                int contactCount = 0;

                for (int i = 0; i < RobotBumpSim.BUMP_LINE_STARTS.length; i++) {
                    double gax = handleCollision(state, currentX, pose.getY(), currentVx, i);
                    if (!Double.isNaN(gax)) {
                        gravAccelXSum += gax;
                        contactCount++;
                    }
                }

                // Floor
                if (state.zPos < 0.0) {
                    state.zPos = 0.0;
                    if (state.zVel < 0.0) state.zVel = -state.zVel * FUEL_COR;
                }

                // Ramp state management (same frictionless model as RobotBumpSim)
                if (contactCount > 0) {
                    if (!state.onRamp) {
                        state.onRamp = true;
                        state.simXPos = pose.getX();
                        state.simXVel = vx;
                    }
                    double avgGravAccelX = gravAccelXSum / contactCount;
                    state.simXVel += avgGravAccelX * dt;
                    state.simXPos += state.simXVel * dt;
                } else if (state.onRamp) {
                    if (state.zPos <= 0.01) {
                        state.onRamp = false;
                    } else {
                        state.simXPos += state.simXVel * dt;
                    }
                }
            }

            // Override dyn4j body when on ramp
            if (state.onRamp) {
                Pose2d corrected = new Pose2d(state.simXPos, pose.getY(), pose.getRotation());
                piece.setTransform(GeometryConvertor.toDyn4jTransform(corrected));
                piece.setLinearVelocity(new Vector2(state.simXVel, vel.y));
            }

            // Build 3D pose with corrected Z
            double visualX = state.onRamp ? state.simXPos : pose.getX();
            double visualZ = Math.max(state.zPos, 0) + FUEL_RADIUS;
            allFuelPoses.add(new Pose3d(
                    visualX,
                    pose.getY(),
                    visualZ,
                    new Rotation3d(0, 0, pose.getRotation().getRadians())));
        }

        // Include airborne fuel projectiles unchanged
        for (GamePiece gp : arena.gamePieceLaunched()) {
            if ("Fuel".equals(gp.getType())) {
                allFuelPoses.add(gp.getPose3d());
            }
        }

        return allFuelPoses.toArray(Pose3d[]::new);
    }

    private double handleCollision(
            FuelState state, double worldX, double worldY, double currentXVel, int lineIdx) {
        Translation3d lineStart = RobotBumpSim.BUMP_LINE_STARTS[lineIdx];
        Translation3d lineEnd = RobotBumpSim.BUMP_LINE_ENDS[lineIdx];

        // Y-range guard
        if (worldY < lineStart.getY() || worldY > lineEnd.getY()) return Double.NaN;

        // Project into XZ plane
        Translation2d start2d = new Translation2d(lineStart.getX(), lineStart.getZ());
        Translation2d end2d = new Translation2d(lineEnd.getX(), lineEnd.getZ());
        Translation2d pos2d = new Translation2d(worldX, state.zPos);
        Translation2d lineVec = end2d.minus(start2d);

        // Closest point on segment (parametric projection)
        Translation2d toFuel = pos2d.minus(start2d);
        double projectionT = toFuel.dot(lineVec) / lineVec.getSquaredNorm();
        Translation2d projected = start2d.plus(lineVec.times(projectionT));

        if (projected.getDistance(start2d) + projected.getDistance(end2d)
                > lineVec.getNorm() + SEGMENT_TOLERANCE) return Double.NaN;

        double dist = pos2d.getDistance(projected);
        if (dist > FUEL_RADIUS) return Double.NaN;

        // Outward normal in XZ
        double normalX = -lineVec.getY() / lineVec.getNorm();
        double normalZ = lineVec.getX() / lineVec.getNorm();

        // Z position correction
        state.zPos += normalZ * (FUEL_RADIUS - dist);

        // Z velocity impulse
        double velDotNormal = currentXVel * normalX + state.zVel * normalZ;
        if (velDotNormal < 0.0) {
            state.zVel += normalZ * (-(1.0 + FUEL_COR) * velDotNormal);
        }

        // Gravity-along-ramp X acceleration
        return -GRAVITY_Z * normalX * normalZ;
    }
}