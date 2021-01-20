var exec = require('cordova/exec');

exports.init = function(success, error) {
    exec(success, error, "SumniPlugin", "init", []);
};
exports.test = function(message, success, error) {
    exec(success, error, "SumniPlugin", "test", [message]);
};
exports.sendFiles = function(packageName, message, paths, success, error) {
    exec(success, error, "SumniPlugin", "sendFiles", [packageName, message, paths]);
};
exports.sendFile = function(packageName, message, path, success, error) {
    exec(success, error, "SumniPlugin", "sendFile", [packageName, message, path]);
};
exports.sendCMD = function(packname, option,message,taskId,model, success, error) {
    exec(success, error, "SumniPlugin", "sendCMD", [packname, option,message,taskId,model]);
};
exports.sendQuery = function(message, packageName, option,type,isReport,fileId, success, error) {
    exec(success, error, "SumniPlugin", "sendQuery", [message, packageName, option,type,isReport,fileId]);
};
exports.checkFileExists = function(fileId,packageName, success, error) {
    exec(success, error, "SumniPlugin", "checkFileExists", [fileId,packageName]);
};
exports.deleteFileExists = function(taskId, success, error) {
    exec(success, error, "SumniPlugin", "deleteFileExists", [taskId]);
};
exports.sendData = function(message, package, option,model,type,taskId, success, error) {
    exec(success, error, "SumniPlugin", "sendData", [message, package, option,model,type,taskId]);
};
exports.getDSDPackageName = function(success, error) {
    exec(success, error, "SumniPlugin", "getDSDPackageName", []);
};
exports.getDSDPackName = function(success, error) {
    exec(success, error, "SumniPlugin", "getDSDPackName", []);
};
exports.checkConnection = function(success, error) {
    exec(success, error, "SumniPlugin", "checkConnection", []);
};