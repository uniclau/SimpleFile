var simpleFile = {
    readFile: function(fileName, successCallback, errorCallback) {
        cordova.exec(
            function(data64) {
                console.log(data64);
                successCallback(atob(data64));
            },
            errorCallback,
            "SimpleFilePlugin",
            "readFile",
            [fileName]
        );
    },
    writeFile: function(fileName, fileData, successCallback, errorCallback) {
        var data64=btoa(fileData);
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "writeFile",
            [fileName, data64]
        );
    },
    deleteFile: function(fileName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "deleteFile",
            [fileName]
        );
    },
    getUrlFile: function(fileName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "getUrlFile",
            [fileName]
        );
    },
    downloadFile: function(url, fileName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "downloadFile",
            [url,fileName]
        );
    },
    createDir: function(dirName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "createDir",
            [dirName]
        );
    },
    deleteDir: function(dirName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "deleteDir",
            [dirName]
        );
    },
    listDir: function(dirName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "listDir",
            [dirName]
        );
    }
};

module.exports = simpleFile;