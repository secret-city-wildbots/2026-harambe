package frc.robot.Utils.simulation;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Meters;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.ironmaple.simulation.seasonspecific.rebuilt2026.Arena2026Rebuilt;
import org.ironmaple.utils.mathutils.GeometryConvertor;

/**
 * Extends Arena2026Rebuilt to fix a copy-paste bug in maple-sim 0.4.0-beta
 * where the red-top trench wall is missing (duplicated blue-bottom instead).
 */
public class FixedArena2026Rebuilt extends Arena2026Rebuilt {

    public FixedArena2026Rebuilt(boolean addRampCollider) {
        super(addRampCollider);

        // The 4th trench wall in Arena2026Rebuilt is a duplicate of the 1st (blue-bottom).
        // Add the missing red-top trench wall.
        double trenchWallDistX = Inches.of(120.0).in(Meters) + Inches.of(47.0 / 2).in(Meters);
        double trenchWallDistY = Inches.of(73.0).in(Meters) + Inches.of(47.0 / 2).in(Meters) + Inches.of(6).in(Meters);

        double redTopX = 8.27 + trenchWallDistX;
        double redTopY = 4.035 + trenchWallDistY;

        Body wall = new Body();
        wall.addFixture(
                Geometry.createRectangle(Inches.of(53).in(Meters), Inches.of(12).in(Meters)));
        wall.setMass(MassType.INFINITE);
        wall.setTransform(GeometryConvertor.toDyn4jTransform(new Pose2d(redTopX, redTopY, Rotation2d.kZero)));

        this.physicsWorld.addBody(wall);
    }
}