modules = {

    app {
        defaultBundle false
//        resource url: '/scss/simple.scss', disposition: 'head', attrs: [type: 'css']
        resource url: '/scss_css/simple.scss.css', disposition: 'head'
//        resource url: '/scss_css/sub/abc.scss.css', disposition: 'head'
    }

    other {
        defaultBundle 'other'
//        resource url: '/css/another.scss', disposition: 'defer'
//        resource url: '/scss/simple.scss', disposition: 'head', attrs: [type: 'css'], bundle: 'scss'
    }
}