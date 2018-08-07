/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
pipeline {
  agent none
  stages {
    stage ('Build') {
      parallel {
        stage ('Build Java 8') {
          agent {
            docker {
              image 'maven:3-jdk-8'
              //label 'ubuntu'
              args '-v /var/lib/jenkins/.m2:/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2'
            }
          }
          steps {
            sh 'mvn -f pom-first.xml clean install -Duser.home=/var/maven'
            sh 'mvn clean install -Duser.home=/var/maven'
          }
        }
/*
        stage ('Build Java 11') {
          agent {
            docker {
              image 'maven:3-jdk-11'
              //label 'ubuntu'
            }
          }
          steps {
            //sh './build.sh'
            sh 'mvn -f pom-first.xml clean install'
          }
        }
*/
      }
    }
  }
}

