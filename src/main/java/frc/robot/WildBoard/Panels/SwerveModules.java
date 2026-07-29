package frc.robot.WildBoard.Panels;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import frc.robot.WildBoard.WBPanel;

public class SwerveModules extends WBPanel {
    MathContext mc = new MathContext(3);

    public SwerveModules() {
        this.usesML = true;

        this.setPanelName("SwerveModules");
    }

    public void updateVals(double[] poses_deg, double[] temps_C, double[] vel_mPs) {
        this.ml.send(Stream.of(poses_deg, temps_C, vel_mPs)
                .flatMapToDouble(Arrays::stream)
                .mapToObj(d -> new BigDecimal(d, this.mc).stripTrailingZeros().toPlainString())
                .collect(Collectors.joining(",")));
    }
}