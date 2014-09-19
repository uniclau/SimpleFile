var simpleFile = {
    read: function(fileName, successCallback, errorCallback) {
        cordova.exec(
            function(data64) {
                console.log(data64);
                successCallback(atob(data64));
            },
            errorCallback,
            "SimpleFilePlugin",
            "read",
            [fileName]
        );
    },
    write: function(fileName, fileData, successCallback, errorCallback) {
        var data64=btoa(fileData);
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "write",
            [fileName, data64]
        );
    },
    remove: function(fileName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "remove",
            [fileName]
        );
    },
    getUrl: function(fileName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "getUrl",
            [fileName]
        );
    },
    download: function(url, fileName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "download",
            [url,fileName]
        );
    },
    createFolder: function(dirName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "createFolder",
            [dirName]
        );
    },
    list: function(dirName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "list",
            [dirName]
        );
    }
};

module.exports = simpleFile;