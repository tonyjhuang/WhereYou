
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

Parse.Cloud.define("checkName", function(request, response) {
  Parse.Cloud.useMasterKey(); 
  var query = new Parse.Query(Parse.Installation);
  var name = request.params.name;
  query.equalTo("nameLowercase", name.toLowerCase());
  query.count({
        success: function(count){
            if(count > 0) {
              response.success(true);
            } else {
              response.success(false);
            }
        },
        error: function(error){
            response.error(error);
        }       
    });
});