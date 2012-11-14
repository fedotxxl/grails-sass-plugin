modules = {

    app {
        resource url: '/scss/simple.scss', disposition: 'head', attrs: [type: 'css']
        resource url: '/css/errors.css', disposition: 'head', attrs: [type: 'css']
    }

    other {
        defaultBundle 'other'
        resource url: '/css/another.scss', disposition: 'head', attrs: [type: 'css'], bundle: 'scss'
        resource url: '/scss/simple.scss', disposition: 'head', attrs: [type: 'css'], bundle: 'scss'
        resource url: '/css/errors.css', disposition: 'head'
        resource url: '/css/main.css', disposition: 'head'
    }
}