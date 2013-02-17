modules = {

    app {
        resource url: '/scss/simple.scss', disposition: 'head', attrs: [type: 'css']
    }

    other {
        defaultBundle 'other'
        resource url: '/css/another.scss', disposition: 'defer'
        resource url: '/scss/simple.scss', disposition: 'head', attrs: [type: 'css'], bundle: 'scss'
    }
}