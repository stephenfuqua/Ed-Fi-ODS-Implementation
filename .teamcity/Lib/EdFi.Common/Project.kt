// SPDX-License-Identifier: Apache-2.0
// Licensed to the Ed-Fi Alliance under one or more agreements.
// The Ed-Fi Alliance licenses this file to you under the Apache License, Version 2.0.
// See the LICENSE and NOTICES files in the project root for more information.

package lib.edFiCommon

import jetbrains.buildServer.configs.kotlin.v2019_2.*

object EdFiCommonProject : Project({
    name = "EdFi.Common"
    id = RelativeId("EdFiCommon")
    description = "Build configurations for the EdFi.Common shared library"

    buildType(lib.edFiCommon.buildTypes.PullRequestBuild)
    buildType(lib.edFiCommon.buildTypes.BranchBuild)

    params {
        param("project.name", "EdFi.Common")
        param("version.core", "%api.semantic.version%")
    }
})
