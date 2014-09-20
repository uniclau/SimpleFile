function FileSystem(root ) {
    var self = this;
    self.root = root;
    self.read= function(fileName, successCallback, errorCallback) {
        cordova.exec(
            function(data64) {
                console.log(data64);
                successCallback(atob(data64));
            },
            errorCallback,
            "SimpleFilePlugin",
            "read",
            [self.root,fileName]
        );
    };
    self.write= function(fileName, fileData, successCallback, errorCallback) {
        var data64=btoa(fileData);
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "write",
            [self.root, fileName, data64]
        );
    };
    self.remove= function(fileName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "remove",
            [self.root, fileName]
        );
    };
    self.getUrl= function(fileName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "getUrl",
            [self.root, fileName]
        );
    };
    self.download= function(url, fileName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "download",
            [self.root,url,fileName]
        );
    };
    self.createFolder= function(dirName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "createFolder",
            [self.root,dirName]
        );
    };
    self.list= function(dirName, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "SimpleFilePlugin",
            "list",
            [self.root,dirName]
        );
    };
    return self;
}



var simpleFile = {
    internal: new FileSystem("internal"),
    external: new FileSystem("external"),
    bundle: new FileSystem("bundle")
};

console.log("SimpleFileCalled");

module.exports = simpleFile;