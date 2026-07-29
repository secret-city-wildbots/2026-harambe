package frc.robot.WildBoard;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotBase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FrontendBuilder {

    public static void buildFrontend() {
        File deployDir = Filesystem.getDeployDirectory();
        File frontendDir = new File(deployDir, "WildBoard/frontend");

        File tmp = RobotBase.isSimulation()
                ? new File(Filesystem.getOperatingDirectory(), "sim/tmp")
                : new File("/tmp");

        File wildboardHome = RobotBase.isSimulation()
                ? new File(Filesystem.getOperatingDirectory(), "sim/home")
                : new File("/home/lvuser/WildBoard");

        tmp.mkdirs();
        wildboardHome.mkdirs();

        File esbuildSrc = new File(frontendDir, "src/esbuild");
        File esbuildTmp = new File(wildboardHome, "esbuild");

        try {
            boolean copy = true;

            if (esbuildTmp.exists() && esbuildTmp.length() == esbuildSrc.length()) {
                copy = false;
            }

            if (copy) {
                Files.copy(esbuildSrc.toPath(), esbuildTmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                esbuildTmp.setExecutable(true);
            }

        } catch (IOException e) {
            System.err.println("Failed to copy esbuild to /tmp: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        File outputDir = new File(wildboardHome, "frontend-public/dynamic");
        outputDir.mkdirs();

        File indexLoader = new File(wildboardHome, "frontend/src/pages/indexLoader.tsx");

        if (RobotBase.isSimulation()) {
            esbuildTmp = new File(Filesystem.getOperatingDirectory(), "esbuild.exe");
        }

        ProcessBuilder pb = new ProcessBuilder(
                esbuildTmp.getAbsolutePath(),
                indexLoader.getAbsolutePath(),
                "--bundle",
                "--outfile=" + new File(outputDir, "index.js").getAbsolutePath(),
                "--format=esm",
                "--platform=browser",

                "--jsx-factory=h",
                "--jsx-fragment=Fragment",
                "--jsx=transform",

                "--minify",
                "--tree-shaking=true",

                "--target=es2020",

                "--log-level=debug"
        );

        pb.directory(frontendDir);
        pb.redirectErrorStream(true);

        // Limit Go runtime memory spikes (important on roboRIO)
        //pb.environment().put("GOGC", "50");

        try {
            Process process = pb.start();
            process.getInputStream().transferTo(System.out);
            int exitCode = process.waitFor();
            System.out.println("Frontend build complete (exit " + exitCode + ")");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}