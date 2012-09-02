
require 'buildr/scala'

# Version number for this release
VERSION_NUMBER = "0.2"
# Group identifier for your projects
GROUP = "tuner"
COPYRIGHT = "Tom Torsney-Weir"

# The main class to run
MAIN_CLASS = "tuner.Tuner"

# Where are the native opengl libs
OPENGL_PATH = "lib/opengl/macosx"

# Where is R
ENV['R_HOME'] = '/Library/Frameworks/R.framework/Resources'

# Where is JRI
JRI_PATH = "/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri"

# Specify Maven 2.0 remote repositories here, like this:
repositories.remote << "http://repo1.maven.org/maven2"

# All the dependencies
DATESCALA = "org.bitbucket.gabysbrain:datescala_#{Scala.version}:jar:0.9"
SCALASWING = "org.scala-lang:scala-swing:jar:#{Scala.version}"
LIFT = transitive("net.liftweb:lift-json_#{Scala.version}:jar:2.4")
TABLE_LAYOUT = "tablelayout:TableLayout:jar:20050920"
COMMONS_MATH = "org.apache.commons:commons-math:jar:2.2"
PREFUSE = "org.prefuse:prefuse:jar:beta-20060220"
JAPURA = 'org.japura:japura:jar:1.15.1'

# special artifact downloads
download artifact(JAPURA) => 'http://downloads.sourceforge.net/project/japura/Japura/Japura%20v1.15.1/japura-1.15.1.jar?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Fjapura%2Ffiles%2F&ts=1343327730&use_mirror=iweb'

# all the artifacts we need
jar_deps = [
  DATESCALA, SCALASWING, LIFT, TABLE_LAYOUT, COMMONS_MATH, PREFUSE, JAPURA
]

desc "The Tuner project"
define "tuner" do

  project.version = VERSION_NUMBER
  project.group = GROUP
  manifest["Implementation-Vendor"] = COPYRIGHT

  # compilation info
  compile.using :deprecation => true
  compile.with jar_deps, Dir[_("lib/*.jar")]

  resources
  test.resources

  # packaging instructions
  package(:jar).with :manifest => manifest.merge('Main-Class' => MAIN_CLASS)

  # set up the jnlp deployment file
  task :jnlp => package(:jar) do
    File.open('target/tuner.jnlp', 'w') do |file|
      xml = Builder::XmlMarkup.new(:target => file, :indent => 2)
      xml.instruct!
      xml.jnlp(:spec=>"1.0+", :codebase=>"here", :href=>"tuner.jnlp") do
        # basic developer/project information
        xml.information do
          xml.title "Tuner"
          xml.vendor COPYRIGHT
          xml.homepage "http://www.tomtorsneyweir.com/tuner"
          xml.icon(:href=>"tuner_icon.png")
          xml.tag! "offline-allowed"
        end
        
        # Everything this project uses
        xml.resources do
          xml.j2se(:version=>"1.6+", :href=>"http://java.sun.com/products/autodl/j2se")
          xml.jar(:href=>"tuner-0.2.jar", :main=>"true")

          # Other jars we need
          
          # Other local extensions
          xml.extension(:name=>"p5", :href=>"p5.jnlp")
          xml.extension(:name=>"p5-opengl", :href=>"p5-opengl.jnlp")
        end

        # Application information
        xml.tag! "application-desc", "name"=>"Tuner application", 
                                     "main-class"=>MAIN_CLASS
      end
    end
  end
  
  # Running instructions
  run.using :main => MAIN_CLASS,
            :java_args => ["-Djava.library.path=#{JRI_PATH}:#{OPENGL_PATH}",
                           "-Xmx512M"]
end
