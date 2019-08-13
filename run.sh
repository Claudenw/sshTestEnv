if [ -z $1 ]
then
    echo "configuration file must be provided"
    exit 1
fi

java -classpath ./target/\* org.xenei.test.testSSH.SSHTestingEnvironment -c $1
