# config valid for current version and patch releases of Capistrano
#lock "~> 3.11.2"

########################
# Setup project
########################
set :application, "l2j-clasic"
set :repo_url, "git@github.com:andreitode/l2j-clasic.git"

#########################
# Setup Capistrano
#########################
set :format, :airbrussh
set :log_level, :debug
set :use_sudo, false
set :keep_releases, 2
# set :linked_dirs, ["./storage"]
# set :app_config_path, 'config/'
# set :format_options, log_file: "storage/logs/capistrano.log"

before 'deploy', 'deploy:confirm:confirmCommits'

namespace :deploy do
    task "migrate" do
        on roles(:main) do
            execute "cd #{release_path} && npx prisma db push"
        end
    end # end task migrate

	namespace "confirm" do
		task "confirmCommits" do
			puts "Changes to be deployed:"
			invoke "deploy:confirm:diff"
			set :confirmation, ask('Do you really want to deploy this changes?', "yes/no")
			print fetch(:confirmation)
			if fetch(:confirmation) == "yes"
				puts "You got it buddy. Imma deploy now."
				invoke "deploy:confirm:ok"
			else
				puts "Whoa whoa whoa! Ok. Good thing I asked!"
				puts "Stopping!"
				invoke "deploy:confirm:notok"
				exit
			end
		end
		task "diff" do
			run_locally do
				%('git fetch --all')
			end
			Capistrano::Pending::SCM.load(fetch(:scm))
			commits = %x(git log #{fetch(:revision)}..origin/#{fetch(:branch)} --format=\"%h %s <%ae>\")
			commits.each_line do |line|
				puts line
			end
		end
		before "diff", "deploy:pending:setup"
		task :ok do
		end
		task :notok do
		end
	end # end namespace confirm

end # end namespace deploy

namespace :build do
    task :ant do
        on roles(:main) do
            puts "*** ANT BUILD"
            execute "cd #{release_path} && ant"
        end
    end
    task :unzip do
        on roles(:main) do
            puts "*** UNZIP BUILD"
            execute "cd #{release_path} && unzip l2j_build.zip"
        end
    end
end

namespace :clean do
    task :move do
        on roles(:main) do
            execute "cd #{release_path}/build && mv l2j_build.zip ../l2j_build.zip"
        end
    end
    task :pre_unzip do
        on roles(:main) do
            execute "cd #{release_path} && rm -rf build build.xml config dist Capfile Gemfile java launcher"
        end
    end
    task :post_unzip do
        on roles(:main) do
            execute "cd #{release_path} && rm -rf l2j_build.zip"
        end
    end
end

namespace :permissions do
    task :login do
        on roles(:main) do
            execute "cd #{release_path}/login && sudo chmod 755 LoginServer.sh LoginServerTask.sh"
        end
    end
    task :game do
        on roles(:main) do
            execute "cd #{release_path}/game && sudo chmod 755 GameServer.sh GameServerTask.sh"
        end
    end
end

namespace :logs do
    task :login do
        on roles(:main) do
            execute "cd #{release_path}/login && mkdir log"
        end
    end
    task :game do
        on roles(:main) do
            execute "cd #{release_path}/game && mkdir log"
        end
    end
end

namespace :stop do
    task :login do
        on roles(:main) do
            execute "pgrep -f 'LoginServer' >/dev/null && pkill -TERM -f 'LoginServer' || echo 'LoginServer not running'"
        end
    end
    task :game do
        on roles(:main) do
            execute "pgrep -f 'GameServer' >/dev/null && pkill -TERM -f 'GameServer' || echo 'GameServer not running'"
        end
    end
end

namespace :start do
    task :login do
        on roles(:main) do
            execute "cd #{release_path}/login && (sh LoginServer.sh >/dev/null 2>&1 &)"
        end
    end
    task :game do
        on roles(:main) do
            execute "cd #{release_path}/game && (sh GameServer.sh >/dev/null 2>&1 &)"
        end
    end
end