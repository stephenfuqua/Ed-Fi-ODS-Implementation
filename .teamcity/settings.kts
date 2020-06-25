import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.replaceContent
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetBuild
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetNugetPush
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetPack
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetTest
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.2"

project {

    vcsRoot(EdFiOds)
    vcsRoot(EdFiOdsDeploy)
    vcsRoot(EdFiOdsCommon)

    buildType(EdFiCommon)
    buildType(EdFiAdminDataAccess)
    buildType(EdFiSecurityDataAccess)
    buildType(EdFiInstallerAppCommon)
    buildType(EdFiInstallerWebApi)
    buildType(EdFiInstallerSwaggerUI)
    buildType(EdFiInstallerSandboxAdmin)

    template(BuildAndPackageSharedLibrary)
    template(PackageAndPublishInstallerLibrary)

    params {
        param("git.branch.default", "development")
        param("git.branch.specification", """
            refs/heads/(*)
            refs/(pull/*)/merge
        """.trimIndent())
        param("msbuild.configuration", "Release")
    }
}

object EdFiInstallerAppCommon : BuildType({
    templates(PackageAndPublishInstallerLibrary)
    name = "EdFi.Installer.AppCommon"
    description = "Commonly used PowerShell scripts for supporting Ed-Fi application installations"

    publishArtifacts = PublishMode.SUCCESSFUL

    params {
        param("version", "1.0.0")
        param("project.directory", "Ed-Fi-ODS-Deploy/src/%project.name%")
    }

})

object EdFiInstallerWebApi : BuildType({
    templates(PackageAndPublishInstallerLibrary)
    name = "EdFi.Installer.WebApi"
    description = "PowerShell deployment orchestration for the Web API. Temporary location in Shared Libraries project."

    publishArtifacts = PublishMode.SUCCESSFUL

    params {
        param("version", "1.0.0")
        param("project.directory", "Ed-Fi-ODS-Implementation/scripts/NuGet/%project.name%")
    }

    vcs {
        root(DslContext.settingsRoot, "+:. => Ed-Fi-ODS-Implementation")
    }
})

object EdFiInstallerSwaggerUI : BuildType({
    templates(PackageAndPublishInstallerLibrary)
    name = "EdFi.Installer.SwaggerUI"
    description = "PowerShell deployment orchestration for SwaggerUI. Temporary location in Shared Libraries project."

    publishArtifacts = PublishMode.SUCCESSFUL

    params {
        param("version", "1.0.0")
        param("project.directory", "Ed-Fi-ODS-Implementation/scripts/NuGet/%project.name%")
    }

    vcs {
        root(DslContext.settingsRoot, "+:. => Ed-Fi-ODS-Implementation")
    }
})

object EdFiInstallerSandboxAdmin : BuildType({
    templates(PackageAndPublishInstallerLibrary)
    name = "EdFi.Installer.SandboxAdmin"
    description = "PowerShell deployment orchestration for Sandbox Admin. Temporary location in Shared Libraries project."

    publishArtifacts = PublishMode.SUCCESSFUL

    params {
        param("version", "1.0.0")
        param("project.directory", "Ed-Fi-ODS-Implementation/scripts/NuGet/%project.name%")
    }

    vcs {
        root(DslContext.settingsRoot, "+:. => Ed-Fi-ODS-Implementation")
    }
})

object EdFiAdminDataAccess : BuildType({
    templates(BuildAndPackageSharedLibrary)
    name = "EdFi.Admin.DataAccess"
    description = "Shared classes for accessing the EdFi_Admin database"

    publishArtifacts = PublishMode.SUCCESSFUL

    params {
        param("version.core", "3.4.0")
    }
})

object EdFiCommon : BuildType({
    templates(BuildAndPackageSharedLibrary)
    name = "EdFi.Common"
    description = "Shared classes in namespace EdFi.Ods.Common"

    publishArtifacts = PublishMode.SUCCESSFUL

    params {
        param("version.core", "3.4.0")
    }
})

object EdFiSecurityDataAccess : BuildType({
    templates(BuildAndPackageSharedLibrary)
    name = "EdFi.Security.DataAccess"
    description = "Shared classes for accessing the EdFi_Security database"

    publishArtifacts = PublishMode.SUCCESSFUL

    params {
        param("version.core", "3.4.0")
    }
})

object BuildAndPackageSharedLibrary : Template({
    name = "Build and Package Shared Library"

    artifactRules = "**/%project.name%*.nupkg"
    publishArtifacts = PublishMode.SUCCESSFUL

    params {
        param("project.testDirectory", "Ed-Fi-ODS/tests/%project.name%.UnitTests/**")
        param("version.prerelease", "pre%build.counter%")
        param("project.name", "%system.teamcity.buildConfName%")
        param("project.file.sln", "%project.directory%/%project.name%.sln")
        param("project.file.csproj", "%project.directory%/%project.name%.csproj")
        param("version.informational", "%version.core%")
        param("project.rootDirectory", "Ed-Fi-ODS/application/")
        param("project.directory", "%project.rootDirectory%/%project.name%")        
        param("edfi.common.directory", "Ed-Fi-ODS/application/EdFi.Commmon")
    }

    vcs {
        root(EdFiOds, "+:. => Ed-Fi-ODS")
    }

    steps {
        dotnetBuild {
            name = "Build"
            id = "RUNNER_385"
            projects = "%project.file.sln%"
            configuration = "%msbuild.configuration%"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        dotnetTest {
            name = "Test"
            id = "RUNNER_386"
            projects = "%project.file.sln%"
            configuration = "%msbuild.configuration%"
            skipBuild = true
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        dotnetPack {
            name = "Pack Pre-Release"
            id = "RUNNER_387"
            projects = "%project.file.csproj%"
            configuration = "%msbuild.configuration%"
            outputDir = "%teamcity.build.checkoutDir%"
            skipBuild = true
            args = """-p:VersionPrefix=%version.core% --version-suffix "%version.prerelease%""""
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        dotnetPack {
            name = "Pack Release"
            id = "RUNNER_388"
            projects = "%project.file.csproj%"
            configuration = "%msbuild.configuration%"
            outputDir = "%teamcity.build.checkoutDir%"
            skipBuild = true
            args = "-p:VersionPrefix=%version.core%"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        dotnetNugetPush {
            name = "Publish Pre-Release"
            id = "RUNNER_390"
            packages = "%project.name%.%version.core%-%version.prerelease%.nupkg"
            serverUrl = "%myget.feed%"
            apiKey = "%myget.apiKey%"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
    }

    triggers {
        vcs {
            id = "vcsTrigger"
            triggerRules = """
                +:%project.directory%/**
                +:%project.testDirectory%/**
                +:%edfi.common.directory%/**
            """.trimIndent()
        }
    }

    features {
        swabra {
            id = "swabra"
        }
        commitStatusPublisher {
            id = "BUILD_EXT_37"
            vcsRootExtId = "${EdFiOds.id}"
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "%github.accessToken%"
                }
            }
        }
        replaceContent {
            id = "BUILD_EXT_54"
            fileRules = "**/Directory.Build.props"
            pattern = """(<(AssemblyVersion)\s*>).*(<\/\s*\2\s*>)"""
            replacement = "${'$'}1%version.core%.%build.counter%${'$'}3"
        }
        replaceContent {
            id = "BUILD_EXT_55"
            fileRules = "**/Directory.Build.props"
            pattern = """(<(FileVersion)\s*>).*(<\/\s*\2\s*>)"""
            replacement = "${'$'}1%version.core%.%build.counter%${'$'}3"
        }
        replaceContent {
            id = "BUILD_EXT_56"
            fileRules = "**/Directory.Build.props"
            pattern = """(<(InformationalVersion)\s*>).*(<\/\s*\2\s*>)"""
            replacement = "${'$'}1%version.informational%${'$'}3"
        }
    }
})

object PackageAndPublishInstallerLibrary : Template({
    name = "Build and Package Installer Library"

    artifactRules = "*.nupkg"
    publishArtifacts = PublishMode.SUCCESSFUL

    option("shouldFailBuildOnAnyErrorMessage", "true")    

    params {
        param("version.preReleaseLabel", "pre")
        param("project.name", "%system.teamcity.buildConfName%")
        param("project.shouldPublishPreRelease", "true")
    }

    vcs {
        root(EdFiOdsDeploy, "+:. => Ed-Fi-ODS-Deploy")
        root(EdFiOdsCommon, "+:. => Ed-Fi-Common")
    }

    steps {
        powerShell {
            name = "Build Pre-release and release, publish pre-release package"
            id = "PackageAndPublishInstallerLibrary_PackPreRelease"
            formatStderrAsError = true
            workingDir = "%project.directory%"
            scriptMode = script {
                content = """
                    ${"$"}parameters = @{
                        SemanticVersion = "%version%"
                        BuildCounter = "%build.counter%"
                        PreReleaseLabel = "%version.preReleaseLabel%"
                        Publish = [System.Convert]::ToBoolean("%project.shouldPublishPreRelease%")
                        NuGetFeed = "%myget.feed%"
                        NuGetApiKey = "%myget.apiKey%"
                    }
                    .\build-package.ps1 @parameters
                """.trimIndent()
            }
        }
    }

    triggers {
        vcs {
            id = "vcsTrigger"
            triggerRules = """
                +:%project.directory%/**
            """.trimIndent()
        }
    }

    features {
        swabra {
            id = "swabra"
        }
    }
})

object EdFiOds : GitVcsRoot({
    name = "Ed-Fi-ODS"
    url = "https://github.com/%github.organization%/Ed-Fi-ODS.git"
    branch = "%git.branch.default%"
    branchSpec = """
        refs/heads/(*)
        refs/(pull/*)/merge
    """.trimIndent()
    userNameStyle = GitVcsRoot.UserNameStyle.FULL
    checkoutSubmodules = GitVcsRoot.CheckoutSubmodules.IGNORE
    serverSideAutoCRLF = true
    useMirrors = false
    authMethod = password {
        userName = "%github.username%"
        password = "%github.accessToken%"
    }
})

object EdFiOdsDeploy : GitVcsRoot({
    name = "Ed-Fi-ODS-Deploy"
    url = "https://github.com/%github.organization%/Ed-Fi-ODS-Deploy.git"
    branch = "%git.branch.default%"
    branchSpec = """
        refs/heads/(*)
        refs/(pull/*)/merge
    """.trimIndent()
    userNameStyle = GitVcsRoot.UserNameStyle.FULL
    checkoutSubmodules = GitVcsRoot.CheckoutSubmodules.IGNORE
    serverSideAutoCRLF = true
    useMirrors = false
    authMethod = password {
        userName = "%github.username%"
        password = "%github.accessToken%"
    }
})

object EdFiOdsCommon : GitVcsRoot({
    name = "Ed-Fi-Common"
    url = "https://github.com/%github.organization%/Ed-Fi-Common.git"
    branch = "%git.branch.default%"
    branchSpec = """
        refs/heads/(*)
        refs/(pull/*)/merge
    """.trimIndent()
    userNameStyle = GitVcsRoot.UserNameStyle.FULL
    checkoutSubmodules = GitVcsRoot.CheckoutSubmodules.IGNORE
    serverSideAutoCRLF = true
    useMirrors = false
    authMethod = password {
        userName = "%github.username%"
        password = "%github.accessToken%"
    }
})
