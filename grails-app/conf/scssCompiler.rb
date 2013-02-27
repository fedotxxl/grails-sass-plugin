#require 'rubygems'
#require 'sass'
#require 'sass/exec'
require 'sass-3.2.3/lib/sass'

def compileSingleScss(template, params, loads_paths)

  convertedParams = Hash.new
=begin

  params.each do |key, value|
    convertedParams[key.to_sym] = value.to_sym
  end
=end

  convertedParams[:cache] = false
  convertedParams[:load_paths] = loads_paths
  convertedParams[:debug_info] = params['debug_info']
  convertedParams[:line_comments] = params['line_comments']
  convertedParams[:style] = params['style'].to_sym
  convertedParams[:syntax] = params['syntax'].to_sym

  sass = Sass::Engine.new(template, convertedParams)
  sass.render
end