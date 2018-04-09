pipeline {
    agent any
	
	parameters {
         string(name: 'tomcat_dev', defaultValue: '10.137.0.180', description: 'Staging Server')
    }
	
    tools {
		maven "MAVEN_HOME"
		jdk "JAVA_HOME"
    }
    stages{
            stage('Preparation'){
    			steps {
    				  git changelog: false, poll: false, url: 'git://github.com/rohitjain994/maven-project.git', branch: 'master'
                }
            }
            stage('Build'){
                steps {
                    sh 'mvn clean package'
                }
                post {
                    success {
                        echo 'Now Archiving...'
                        archiveArtifacts artifacts: '**/target/*.war'
                    }
                }
            }
    
            stage ('Deployments'){
                parallel{
                    stage ('Deploy to Staging'){
                        steps {
    							sh "sudo scp -i /home/ec2-user/SRID.pem -o StrictHostKeyChecking=no **/target/*.war ec2-user@${params.tomcat_dev}:/home/ec2-user/apache-tomcat-8.0.50/webapps"
    							sh "ssh -i /home/ec2-user/SRID.pem -o StrictHostKeyChecking=no ec2-user@${params.tomcat_dev} sudo sh /home/ec2-user/apache-tomcat-8.0.50/bin/shutdown.sh"
    							sh "ssh -i /home/ec2-user/SRID.pem -o StrictHostKeyChecking=no ec2-user@${params.tomcat_dev} sudo sh /home/ec2-user/apache-tomcat-8.0.50/bin/startup.sh"
    						
                        }
                    }
                }
            }
        }
}