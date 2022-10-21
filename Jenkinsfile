#!/usr/bin/env groovy

/* `buildPlugin` step provided by: https://github.com/jenkins-infra/pipeline-library */
/* Check Java 17 and Java 11 on Linux and Java 8 on Windows */
/* Covers all Java versions and both platforms */
buildPlugin(failFast: false,
            // Opt-in to the Artifact Caching Proxy, to be removed when it will be in opt-out.
            // See https://github.com/jenkins-infra/helpdesk/issues/2752 for more details and updates.
            artifactCachingProxyEnabled: true,
            configurations: [
                [platform: 'linux',   jdk: '17', jenkins: '2.342'],
                [platform: 'linux',   jdk: '11'],
                [platform: 'windows', jdk: '8'],
            ])
