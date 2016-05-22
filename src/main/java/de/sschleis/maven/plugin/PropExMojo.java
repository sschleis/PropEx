package de.sschleis.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Goal which touches a timestamp file.
 *
 * @goal propex
 * @phase install
 */
public class PropExMojo extends AbstractMojo {

    /**
     * Project's source directory as specified in the POM.
     *
     * @parameter expression="${project.build.sourceDirectory}"
     * @readonly
     * @required
     */
    private final File sourceDirectory = new File("");
    /**
     * List with all files to be counted
     */
    private final List<String> files = new LinkedList<String>();
    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;
    private List<SpringPropertie> properties = new LinkedList<SpringPropertie>();
    private String encoding = "UTF-8";

    public void execute() throws MojoExecutionException, MojoFailureException {

        File f = outputDirectory;
        if (!f.exists()) {
            f.mkdirs();
        }

        if (!sourceDirectory.exists()) {
            getLog().error("Source directory \"" + sourceDirectory + "\" is not valid.");
            return;
        }

        fillListWithAllFilesRecursiveTask(sourceDirectory, files);


        findProperties();

        File touch = new File(f, "propEx.txt");

        FileWriter w = null;
        try {
            w = new FileWriter(touch);
            w.write("Properties found: " + properties.size() + "\n");
            for (SpringPropertie propertie : properties) {
                w.append(propertie.toCsv());
            }
            w.flush();
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + touch, e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private void findProperties() {
        for (String file : files) {
            try {
                final Scanner scan = new Scanner(new File(file), encoding);
                String line = scan.nextLine();
                while (scan.hasNext()) {
                    if (line.contains("@Value")) {
                        final SpringPropertie springPropertie = new SpringPropertie(line);
                        properties.add(springPropertie);
                    }
                    line = scan.nextLine();

                }
            } catch (final IOException e) {
                getLog().error(e.getMessage());
            }
        }
    }

    private void fillListWithAllFilesRecursiveTask(final File root, final List<String> files) {
        if (root.isFile()) {
            files.add(root.getPath());
            return;
        }
        for (final File file : root.listFiles()) {
            if (file.isDirectory()) {
                fillListWithAllFilesRecursiveTask(file, files);
            } else {
                files.add(file.getPath());
            }
        }
    }
}
