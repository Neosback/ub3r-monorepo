plugins { kotlin("jvm"); `java-library` }
dependencies { implementation(project(":skills:api")); implementation(project(":skills:runtime")); testImplementation(project(":skills:testkit")) }
