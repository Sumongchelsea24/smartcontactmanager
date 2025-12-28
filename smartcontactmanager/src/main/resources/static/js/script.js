console.log("This is script page")
const toggleSidebar= ( )=>{

    if($('.sidebar').is(":visible")){
        //true
        //band garnu parxa
        $(".sidebar").css("display","none")
        $(".content").css("margin-left","0")
    }
    else{
        //false
          //show garnu parxa
          $(".sidebar").css("display","block")
          $(".content").css("margin-left","20%")
    }
};