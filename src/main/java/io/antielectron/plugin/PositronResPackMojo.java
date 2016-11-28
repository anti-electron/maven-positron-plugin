package io.antielectron.plugin;

import com.sun.org.apache.regexp.internal.RESyntaxException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * TODO Document
 * @author Evan Geng
 */
@Mojo(name = "respack", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class PositronResPackMojo extends AbstractMojo {

    @Parameter(defaultValue = "src/main/webapp", required = true)
    private String resourceDirectory;

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private String outputDirectory;

    @Parameter(property = "project")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Configuring web resource directory...");
            Resource res = new Resource();
            res.setDirectory(resourceDirectory);
            res.setTargetPath("WebContent");
            project.addResource(res);
        } catch (Exception e) {
            throw new MojoFailureException("Could not confiugre resource directory!", e);
        }

        List<String> resources;
        try {
            getLog().info("Indexing web resources...");
            Path srcPath = FileSystems.getDefault().getPath(resourceDirectory);
            resources = Files.walk(srcPath)
                    .filter(p -> p.toFile().isFile())
                    .map(p -> srcPath.relativize(p).toString())
                    .peek(logTo(getLog()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new MojoFailureException("Could not index resources!", e);
        }

        try {
            getLog().info("Writing to resource index file...");
            Path tgtPath = FileSystems.getDefault().getPath(outputDirectory, "positron-resources.index");
            Files.createDirectories(tgtPath.getParent());
            Files.write(tgtPath, resources);
        } catch (Exception e) {
            throw new MojoFailureException("Could not write index file!", e);
        }
    }

    private static Consumer<? super String> logTo(Log log) {
        return s -> log.info("Indexed: " + s);
    }

}
