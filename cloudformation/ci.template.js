var cf = require('@mapbox/cloudfriend');

module.exports = {
AWSTemplateFormatVersion: '2010-09-09',
  Resources: {
    User: {
      Type: 'AWS::IAM::User',
      Properties: {
        Policies: [
          {
            PolicyName: 'devicefarm',
            PolicyDocument: {
              Statement: [
                {
                  Action: ['devicefarm:*'],
                  Effect: 'Allow',
                  Resource: '*'
                }
              ]
            }
          },
          {
            PolicyName: 'publish-metrics',
            PolicyDocument: {
              Statement: [              
                {
                  Action: ['s3:PutObject'],
                  Effect: 'Allow',
                  Resource: 'arn:aws:s3:::mapbox-loading-dock/raw/mobile_*/*'
                }
              ]  
            }
          }
        ]
      }
    },
    AccessKey: {
      Type: 'AWS::IAM::AccessKey',
      Properties: {
        UserName: cf.ref('User')
      }
    }
  },
  Outputs: {
    AccessKeyId: { Value: cf.ref('AccessKey') },
    SecretAccessKey: { Value: cf.getAtt('AccessKey', 'SecretAccessKey') }
  }
};