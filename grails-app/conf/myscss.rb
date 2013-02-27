#require 'rubygems'
#require 'sass'
#require 'sass/exec'
require 'sass-3.2.3/lib/sass'

def compileScss(filesIn, filesTo)
  puts filesIn
  puts filesTo

  files = Dir.glob(filesIn)
  Dir.mkdir(filesTo) unless File.exists?(filesTo)

  puts files

  files.each do
  | file |
    puts "     [sass compiler] " + file + " -> " + filesTo + "/" + File.basename(file, ".*") + ".css"
    opts = Sass::Exec::Sass.new(["--load-path", File.dirname(file), file, File.join(filesTo, File.basename(file, ".*") + ".css")])
    opts.parse
  end
end

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