var fs = require('fs');
var path = require('path');
var appId = "";


function replacer(match, p1,p2, p3, offset, string){
    return [p1,appId,".R",p3].join("");
}
module.exports = function (context) {
    
    console.log("Start changing Code Files!");
    var Q = context.requireCordovaModule("q");
    var deferral = new Q.defer();


    var rawConfig = fs.readFileSync("config.xml", 'ascii');
    var match = /^<widget[\s|\S]* id="([\S]+)".+?>$/gm.exec(rawConfig);
    if(!match || match.length != 2){
        throw new Error("id parse failed");
    }

    var id = match[1];
    appId = id;
    var regexId = new RegExp("\\.","g");
    id = id.replace(regexId,"/")

    var projectRoot = context.opts.cordova.project ? context.opts.cordova.project.root : context.opts.projectRoot;
    var activityFilePath = path.join(projectRoot,"platforms","android","app","src","main","java","com","outsystems","sumnisdk","present","TextDisplay.java");
    if (fs.existsSync(activityFilePath)) {
        var activity = fs.readFileSync(activityFilePath, "utf8");

        var regex = /([\s|\S]*)(com\.outsystems\.SumniSDKSample.R)([\s|\S]*)/gm;
        activity = activity.replace(regex,replacer);

        
        fs.writeFileSync(activityFilePath, activity);
        console.log("Finished changing the packageNames!");
    }else{
        console.log("Error could not find packageNames!");
    }
    deferral.resolve();

    return deferral.promise;
}