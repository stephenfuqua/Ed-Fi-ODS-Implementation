// SPDX-License-Identifier: Apache-2.0
// Licensed to the Ed-Fi Alliance under one or more agreements.
// The Ed-Fi Alliance licenses this file to you under the Apache License, Version 2.0.
// See the LICENSE and NOTICES files in the project root for more information.

package lib.edFiLoadTools.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*

object PullRequestBuild : BuildType ({
    name = "Pull Request Build and Test"
    id = RelativeId("EdFiLoadTools_PullRequestBuild")

    templates(lib.templates.BuildSharedLibraryPullRequest)

    vcs {
        root(lib.edFiLoadTools.vcsRoots.EdFiStandard, """
            +:Schemas/Bulk => Ed-Fi-Standard/Schemas/Bulk
            +:Samples/Sample XML => Ed-Fi-Standard/Samples/Sample XML
        """.trimIndent())
    }
})
