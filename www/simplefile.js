var simpleFile = {
    getFile: function(fileName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "getFile",
            [fileName]
        );
    },
    setFile: function(fileName, fileData, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "setFile",
            [fileName, fileData]
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
    }
    deleteDir: function(dirName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "deleteDir",
            [dirName]
        );
    }
};

module.exports = alarmPlugin;
