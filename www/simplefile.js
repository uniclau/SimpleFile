function FileSystem(root) {
    var self = this;
    self.root = root;
    self.read= function(fileName, successCallback, errorCallback) {
        cordova.exec(
            function(data64) {
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
    self.getURL= function(fileName, successCallback, errorCallback) {
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
    bundle: new FileSystem("bundle"),
    cache: new FileSystem("cache"),
    tmp: new FileSystem("tmp"),
/*    "copy": function (fromFS, fromItem, toFS, toItem, cb, errcb) {
        window.plugins.simpleFile[fromFS].list(fromItem, function(list) {
            window.plugins.simpleFile[toFS].createFolder(toItem, function() {
                async.eachSeries(list, function(f, cb2) {
                    var newFrom = fromItem + "/" + f.name;
                    var newTo = toItem + "/" + f.name;
                    simpleFile.copy(fromFS, newFrom, toFS, newTo, function() {
                        setTimeout(function() {cb2();}, 50);
                    }, function(err) {
                        setTimeout(function() {cb2(err);}, 50);
                    });
                }, function(err) {
                    if (err) {
                        setTimeout(function() {errcb(err);}, 50);
                    } else {
                        setTimeout(function() {cb();}, 50);
                    }
                });
            }, errcb);
        },function(err) {
            // Might be a file
            window.plugins.simpleFile[fromFS].read(fromItem, function(data) {
                window.plugins.simpleFile[toFS].write(toItem, data, cb, errcb);
            }, errcb);
        });
    } */
    copy: function(fromFS, fromItem, toFS, toItem, cb, errcb) {
        cordova.exec(
            cb,
            errcb,
            "SimpleFilePlugin",
            "copy",
            [fromFS, fromItem, toFS, toItem]
        );
    }
};

module.exports = simpleFile;
