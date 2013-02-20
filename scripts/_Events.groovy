eventStatusUpdate = { msg ->
    println "scss-status-${msg}"
}

eventStatusFinal = { msg ->
    println "scss-status-final-${msg}"
}

eventCompileEnd = { binding ->
    println "scss-status-compile-end"
}

eventCreateWarStart = { name, stagingDir ->
    println "scss-status-create-war"
}
