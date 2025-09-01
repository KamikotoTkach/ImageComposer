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

import lombok.extern.java.Log;
import ru.cwcode.tkach.imagecomposer.config.ComponentConfig;
import ru.cwcode.tkach.imagecomposer.config.CredentialsConfig;
import ru.cwcode.tkach.imagecomposer.config.DeployConfig;
import ru.cwcode.tkach.imagecomposer.config.ImagesConfig;
import ru.cwcode.tkach.imagecomposer.service.*;

import java.nio.file.Path;

@Log
public class ImageComposer {
  public static void main(String[] args) {
    String basedir = "";
    if (args.length == 1) basedir = args[0];
    
    log.info("Working dir: " + Path.of(basedir).toAbsolutePath());
    
    ConfigLoaderService configLoaderService = new ConfigLoaderService(basedir);
    
    ComponentConfig componentConfig = configLoaderService.getComponentConfig();
    ImagesConfig imagesConfig = configLoaderService.getImagesConfig();
    DeployConfig deployConfig = configLoaderService.getDeployConfig();
    CredentialsConfig credentialsConfig = configLoaderService.getCredentialsConfig();
    
    CredentialsService credentialsService = new CredentialsService(credentialsConfig);
    DependencyResolverService dependencyResolverService = new DependencyResolverService(componentConfig);
    ImageBuilderService imageBuilderService = new ImageBuilderService(deployConfig, dependencyResolverService, credentialsService, basedir);
    BuilderService builderService = new BuilderService(imagesConfig, imageBuilderService);
    
    builderService.build();
  }
}
