#!/usr/bin/env groovy

/* `buildPlugin` step provided by: https://github.com/jenkins-infra/pipeline-library */
/* Check Java 11 on Linux and Java 8 on Windows */
/* Covers both Java versions and both platforms */
buildPlugin(failFast: false,
            configurations: [
                [platform: 'linux', jdk: '11'],
                [platform: 'windows', jdk: '8'],
            ])
