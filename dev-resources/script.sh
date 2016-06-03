# INIT

function get_deps {
  cd ~/ &&
  sudo apt-get update &&

  # JAVA #
  sudo apt-add-repository ppa:webupd8team/java -y &&
  sudo apt-get update &&
  echo "oracle-java8-ins taller shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections && # pre-accept license
  sudo apt-get install oracle-java8-installer -y &&
  
  # LEININGEN #
  curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > ~/lein
  sudo chmod a+x ~/lein
  sudo mv ~/lein /bin/
  lein # auto install

  # GIT #
  sudo apt-get install git -y &&

  # NODE #
  sudo apt-get install node -y &&
  sudo apt-get install npm -y &&
  sudo npm install -g npm && # to update NPM to latest version
  sudo rm /usr/sbin/node && sudo ln /usr/bin/nodejs /usr/sbin/node && # http://stackoverflow.com/questions/24721182/when-i-run-node-nothing-happens-the-same-with-forever

  # UTILS #
  sudo apt-get install atop -y &&

  # NPM UTILS #
  sudo npm install -g uglifyjs &&
  sudo npm install -g uglifyify &&
  sudo npm install -g browserify
}

# SSH

function ssh_in {
  chmod 400 "$KEYS_DIR/$KEYFILE.pem" # give keyfile correct permissions
  ssh -i "$KEYS_DIR/$KEYFILE.pem" ubuntu@ec2-<IP>.compute-1.amazonaws.com -t -t
}

# SCREEN

function create_and_goto_screen {
  export TERM=screen.linux && screen -S Main
}

function goto_screen {
  export TERM=screen.linux && screen -Dr Main
}

function kill_screen {
  screen_name=$1
  screen -X -S screen_name kill
}

function dev_launch {
	sudo LEIN_ROOT=1 lein trampoline run -m clojure.main
}

function prod_launch {
  sudo LEIN_ROOT=1 lein with-profile prod trampoline run -m clojure.main
}

# GIT

function clone {
  git clone https://github.com/junto-labs/learnspecter.git
}

function git_repull {
  sudo git clean -df && git checkout -- . && git pull
}
# DEPLOY

function cljsbuild {
  rm -rf ./resources/public/js/ && mkdir ./resources/public/js && sudo lein package
}