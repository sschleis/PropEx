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
public class PropExMojo extends AbstractMojo
{

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
    private List<SpringPropertie> properties = new LinkedList<SpringPropertie>();
    private String encoding = "UTF-8";

    public void execute() throws MojoExecutionException, MojoFailureException
    {

        File f = outputDirectory;
        if (!f.exists())
        {
            f.mkdirs();
        }

        if (!sourceDirectory.exists())
        {
            getLog().error("Source directory \"" + sourceDirectory + "\" is not valid.");
            return;
        }

        fillListWithAllFilesRecursiveTask(sourceDirectory, sourceFiles);
        findProperties(sourceFiles);

        if (!resourcesDirectory.exists())
        {
            getLog().warn("Resource directory \"" + resourcesDirectory + "\" is not valid.");
        }
        else
        {
            fillListWithAllFilesRecursiveTask(resourcesDirectory, resourceFiles);
            findProperties(resourceFiles);
        }

        File touch = new File(f, "propEx.txt");

        FileWriter w = null;
        try
        {
            w = new FileWriter(touch);
            w.write("Properties found: " + properties.size() + "\n");
            for (SpringPropertie propertie : properties)
            {
                w.append(propertie.toCsv());
            }
            w.flush();
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error creating file " + touch, e);
        }
        finally
        {
            if (w != null)
            {
                try
                {
                    w.close();
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
        }
    }

    private void findProperties(final List<String> files)
    {
        for (String file : files)
        {
            try
            {
                final Scanner scan = new Scanner(new File(file), encoding);
                String line = scan.nextLine().trim();
                while (scan.hasNext())
                {
                    try
                    {
                        if (line.contains("${"))
                        {
                            cutProperties(line);
                        }
                    }
                    catch (Exception e)
                    {
                        getLog().warn(line, e);
                    }
                    line = scan.nextLine();

                }
            }
            catch (final IOException e)
            {
                getLog().error(e.getMessage());
            }
        }
    }

    private void cutProperties(final String line)
    {
        final int start = line.indexOf("${");
        final int end = line.indexOf("}");
        final String substring = line.substring(start + 2, end);
        if (substring.contains(":"))
        {
            final int dp = substring.indexOf(":");
            String name = substring.substring(0, dp);
            String defaultValue = substring.substring(dp + 1, substring.length());
            if (checkPropertie(name))
            {
                properties.add(new SpringPropertie(name, null, defaultValue, null));
            }
        }
        else
        {
            if (checkPropertie(substring))
            {
                properties.add(new SpringPropertie(substring, null, null, null));
            }
        }

        final String endLine = line.substring(end + 1, line.length());
        if (endLine.contains("${"))
        {
            cutProperties(endLine);
        }
    }

    private boolean checkPropertie(final String prop){
        boolean result = true;
        if(prop.matches("[A-Z_]*"))
            result = false;

        return result;
    }

    private void fillListWithAllFilesRecursiveTask(final File root, final List<String> files)
    {
        if (root.isFile())
        {
            files.add(root.getPath());
            return;
        }
        for (final File file : root.listFiles())
        {
            if (file.isDirectory())
            {
                fillListWithAllFilesRecursiveTask(file, files);
            }
            else
            {
                files.add(file.getPath());
            }
        }
    }
}
