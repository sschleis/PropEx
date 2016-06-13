package de.sschleis.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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
     * Project's resources directory as specified in the POM.
     *
     * @parameter expression="${resources.main.dir}"
     * @readonly
     * @required
     */
    private final File resourcesDirectory = new File("");

    /**
     * List with all srource Files to be counted
     */
    private final List<String> sourceFiles = new LinkedList<String>();

    /**
     * List with all resource Files to be counted
     */
    private final List<String> resourceFiles = new LinkedList<String>();

    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter expression="${project.artifactId}
     * @required
     */
    private String artifactId;

    /**
     * @parameter expression="${project.version}"
     * @requiered
     */
    private String version;

    private Map<String, SpringPropertie> properties = new HashMap<String, SpringPropertie>();
    private String encoding = "UTF-8";

    public void execute() throws MojoExecutionException, MojoFailureException {

        getLog().info(artifactId);

        File f = outputDirectory;
        if (!f.exists()) {
            f.mkdirs();
        }

        if (!sourceDirectory.exists()) {
            getLog().error("Source directory \"" + sourceDirectory + "\" is not valid.");
            return;
        }

        fillListWithAllFilesRecursiveTask(sourceDirectory, sourceFiles);
        findProperties(sourceFiles);

        if (!resourcesDirectory.exists()) {
            getLog().warn("Resource directory \"" + resourcesDirectory + "\" is not valid.");
        } else {
            fillListWithAllFilesRecursiveTask(resourcesDirectory, resourceFiles);
            findProperties(resourceFiles);
        }

        String fileNameTXT = artifactId + "-" + version + "-" + "propEx.txt";
        String fileNameADOC = artifactId + "-" + version + "-" + "propEx.adoc";

        File touchTXT = new File(f, fileNameTXT);
        File touchADOC = new File(f, fileNameADOC);

        FileWriter writerTXT = null;
        FileWriter writerADOC = null;
        try {
            writerTXT = new FileWriter(touchTXT);
            writerTXT.write("Properties found: " + properties.size() + "\n");
            for (SpringPropertie propertie : properties.values()) {
                writerTXT.append(propertie.toCsv());
            }

            writerADOC = new FileWriter(touchADOC);
            adocHeader(writerADOC);
            adocBody(writerADOC, properties.values());
            adocFooter(writerADOC);

            writerTXT.flush();
            writerADOC.flush();
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + touchTXT, e);
        } finally {
            if (writerTXT != null) {
                try {
                    writerTXT.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private void adocHeader(FileWriter w) throws IOException {
        w.write("[width=\"60%\",frame=\"topbot\",options=\"header\"]\n");
        w.write("|======================\n");
        w.write("|Propertie |Default\n");
    }

    private void adocFooter(FileWriter w) throws IOException {
        w.write("|======================\n");
    }

    private void adocBody(FileWriter w, Collection<SpringPropertie> list) throws IOException {
        for(SpringPropertie propertie : list){
            String defaultValue = propertie.getDefaultValue() != null ? propertie.getDefaultValue() : "";
            w.write("|" + propertie.getName() + " |" + defaultValue + "\n");
        }
    }


    private void findProperties(final List<String> files) {
        for (String file : files) {
            try {
                final Scanner scan = new Scanner(new File(file), encoding);
                String line = scan.nextLine().trim();
                while (scan.hasNext()) {
                    try {
                        if (line.contains("${")) {
                            cutProperties(line);
                        }
                    } catch (Exception e) {
                        getLog().warn(line, e);
                    }
                    line = scan.nextLine();

                }
            } catch (final IOException e) {
                getLog().error(e.getMessage());
            }
        }
    }

    private void cutProperties(final String line) {
        final int start = line.indexOf("${");
        final int end = line.indexOf("}");
        final String substring = line.substring(start + 2, end);
        if (substring.contains(":")) {
            final int dp = substring.indexOf(":");
            String name = substring.substring(0, dp);
            String defaultValue = substring.substring(dp + 1, substring.length());
            if (checkPropertie(name)) {
                properties.put(name, new SpringPropertie(name, null, defaultValue, null));
            }
        } else {
            if (checkPropertie(substring)) {
                properties.put(substring, new SpringPropertie(substring, null, null, null));
            }
        }

        final String endLine = line.substring(end + 1, line.length());
        if (endLine.contains("${")) {
            cutProperties(endLine);
        }
    }

    private boolean checkPropertie(final String prop) {
        boolean result = true;
        if (prop.matches("[A-Z_]*"))
            result = false;

        return result;
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
