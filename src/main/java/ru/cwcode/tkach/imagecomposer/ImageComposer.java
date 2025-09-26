/*
 * Docker image composer, 'ImageComposer'
 * Copyright (c) 2025. Danil Tkachenko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * E-mail tkach@cwcode.ru
 *
 */

package ru.cwcode.tkach.imagecomposer;

import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;
import ru.cwcode.tkach.imagecomposer.config.*;
import ru.cwcode.tkach.imagecomposer.service.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

@Log
@Getter
public class ImageComposer {
  ConfigLoaderService configLoaderService;
  BuildDataConfig buildDataConfig;
  ComponentConfig componentConfig;
  ImagesConfig imagesConfig;
  DeployConfig deployConfig;
  CredentialsConfig credentialsConfig;
  NotifyConfig notifyConfig;
  CredentialsService credentialsService;
  DependencyResolverService dependencyResolverService;
  ImageBuilderService imageBuilderService;
  BuilderService builderService;
  UpdateCheckerService updateCheckerService;
  LogService logService;
  
  String workingDirectory;
  
  public static void main(String[] args) {
    try {
      new ImageComposer().start(args);
    } catch (Throwable e) {
      log.severe("Exception: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  public void setup() {
    configLoaderService = new ConfigLoaderService(workingDirectory);
    
    buildDataConfig = configLoaderService.getLastBuildConfig();
    componentConfig = configLoaderService.getComponentConfig();
    imagesConfig = configLoaderService.getImagesConfig();
    deployConfig = configLoaderService.getDeployConfig();
    credentialsConfig = configLoaderService.getCredentialsConfig();
    notifyConfig = configLoaderService.getNotifyConfig();
    
    logService = new LogService(notifyConfig);
    credentialsService = new CredentialsService(credentialsConfig);
    dependencyResolverService = new DependencyResolverService(componentConfig);
    imageBuilderService = new ImageBuilderService(deployConfig, dependencyResolverService, credentialsService, workingDirectory, logService);
    updateCheckerService = new UpdateCheckerService(dependencyResolverService, buildDataConfig, workingDirectory, configLoaderService, logService);
    builderService = new BuilderService(imagesConfig, imageBuilderService, updateCheckerService, logService);
  }
  
  private void start(String[] args) throws IOException, org.apache.commons.cli.ParseException {
    workingDirectory = Path.of("").toAbsolutePath().toString();
    
    Options options = new Options();
    
    options.addOption(Option.builder("d")
                            .longOpt("directory")
                            .hasArg()
                            .argName("path")
                            .desc("Working directory")
                            .get());
    
    options.addOption(Option.builder("h")
                            .longOpt("help")
                            .desc("Show help")
                            .get());
    
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);
    
    if (cmd.hasOption("help")) {
      HelpFormatter.builder().get().printHelp("java -jar app.jar build-all|build-updated|build <image>", null, options, null, false);
      return;
    }
    
    if (cmd.hasOption("directory")) {
      workingDirectory = cmd.getOptionValue("directory");
    }
    
    System.out.println("Working directory: " + workingDirectory);
    
    String[] remainingArgs = cmd.getArgs();
    if (remainingArgs.length == 0) {
      System.out.println("No command specified. Use: build-all, build <name>, or build-updated");
      return;
    }
    
    String command = remainingArgs[0];
    switch (command) {
      case "build-all":
        buildAll();
        break;
      case "build":
        if (remainingArgs.length < 2) {
          System.out.println("Please specify name for build command");
          return;
        }
        build(remainingArgs[1]);
        break;
      case "build-updated":
        buildUpdated();
        break;
      default:
        System.out.println("Unknown command: " + command);
    }
  }
  
  private void buildAll() {
    setup();
    
    logService.log(Level.FINE, "Building all images");
    builderService.buildAll();
    
    updateCheckerService.runOnDisableTasks();
    configLoaderService.setLastBuildConfig(buildDataConfig);
    logService.send();
  }
  
  private void build(String name) {
    setup();
    
    builderService.build(name);
    
    updateCheckerService.runOnDisableTasks();
    configLoaderService.setLastBuildConfig(buildDataConfig);
    logService.send();
  }
  
  private void buildUpdated() {
    setup();
    
    logService.log(Level.FINE, "Building updated images");
    builderService.buildUpdated();
    
    updateCheckerService.runOnDisableTasks();
    configLoaderService.setLastBuildConfig(buildDataConfig);
    logService.send();
  }
}
