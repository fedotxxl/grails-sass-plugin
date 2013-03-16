require 'gems/sass-3.2.3/lib/sass'

def compileSingleScss(template, params, load_paths)

  convertedParams = Hash.new

  convertedParams[:cache] = false
  convertedParams[:load_paths] = load_paths
  convertedParams[:debug_info] = params['debug_info']
  convertedParams[:line_comments] = params['line_comments']
  convertedParams[:style] = params['style'].to_sym
  convertedParams[:syntax] = params['syntax'].to_sym

  sass = Sass::Engine.new(template, convertedParams)
  sass.render
end