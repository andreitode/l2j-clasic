#######################
# Setup Server
########
################
server "199.247.6.50", user: "root", roles: %w{ app main web db cron }

set :deploy_to, "/var/www/"
set :keep_releases, 2
# set :linked_files, ['.env']

#########################
# Setup Git
#########################
set :branch, ENV['BRANCH'] || proc { `git rev-parse --abbrev-ref HEAD`.chomp }

after "deploy:set_current_revision", 'build:ant'
# after "build:ant", 'clean:move'
# after "clean:move", 'clean:pre_unzip'
# after "clean:pre_unzip", 'build:unzip'
# after "build:unzip", 'clean:post_unzip'
# after "clean:post_unzip", 'permissions:login'
# after "permissions:login", 'permissions:game'
# after "permissions:game", 'logs:login'
# after "logs:login", 'logs:game'
# after "logs:game", 'stop:game'
# after "stop:game", 'stop:login'
# after "stop:login", 'start:login'
# after "stop:game", 'start:game'