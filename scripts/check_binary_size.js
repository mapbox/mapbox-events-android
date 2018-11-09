#!/usr/bin/env node

const jwt = require('jsonwebtoken');
const github = require('@octokit/rest')();
const prettyBytes = require('pretty-bytes');
const AWS = require('aws-sdk');
const zlib = require('zlib');
const fs = require('fs');

const SIZE_CHECK_APP_ID = 14028;
const SIZE_CHECK_APP_INSTALLATION_ID = 229425;

const file = process.argv[2];
const label = process.argv[3];
const {size} = fs.statSync(file);

process.on('unhandledRejection', error => {
    console.log(error);
    process.exit(1)
});

const key = Buffer.from(process.env['SIZE_CHECK_APP_PRIVATE_KEY'], 'base64').toString('binary');
const payload = {
    exp: Math.floor(Date.now() / 1000) + 60,
    iat: Math.floor(Date.now() / 1000),
    iss: SIZE_CHECK_APP_ID
};

const token = jwt.sign(payload, key, {algorithm: 'RS256'});
github.authenticate({type: 'app', token});

github.apps.createInstallationToken({installation_id: SIZE_CHECK_APP_INSTALLATION_ID})
    .then(({data}) => {
        github.authenticate({type: 'token', token: data.token});
        return github.checks.create({
            owner: 'mapbox',
            repo: 'mapbox-events-android',
            name: `Size - ${label}`,
            head_branch: process.env['CIRCLE_BRANCH'],
            head_sha: process.env['CIRCLE_SHA1'],
            status: 'completed',
            conclusion: 'success',
            completed_at: new Date().toISOString(),
            output: {
                title: prettyBytes(size),
                summary: `\`${file}\` is ${size} bytes (${prettyBytes(size)})`
            }
        });
    });

const json = formatJson(size, "Android-Total");
const awsKey = generateKey();

console.log(json);

publish(json, awsKey);

function publish(json, awsKey) {
    new AWS.S3({region: 'us-east-1'}).putObject({
                    Body: zlib.gzipSync(JSON.stringify(json)),
                    Bucket: 'mapbox',
                    Key: awsKey,
                }).promise();
}

function formatJson(size, platform) {
    const json = {};
    json.sdk = "telemetry";
    json.platform = platform;
    json.size = size;
    json.created_at = new Date().toISOString();

    return formatJson;
}

function generateKey() {
    const dateString = new Date().toISOString().split('T')[0]
    const key = 'mapbox-loading-dock/raw/mobile.binarysize/' + dateString + '/android_total_size.json.gz';

    return key;
}