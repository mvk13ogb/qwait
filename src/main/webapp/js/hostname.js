function capitaliseFirstLetter(string){
    return string.charAt(0).toUpperCase() + string.slice(1);
}

function insertSpace(string){
    
    //Removes every hyphen
    string = string.replace(/-/g, "");
    
    //Splits the string before every digit
    string = string.split(/(?=\d)/);

    //Returns the first element from the split, adds a space and returns the rest
    return string[0] + " " + string.splice(1).join().replace(",", "");
}

//Returns everything before the first dot
function splitAtDot(string){
    return string.split(".", 1)[0];
}

//Returns the computer name if we recognize it, otherwise returns empty string
function getComputerName(){

    //If the location already exists
    if(document.getElementById('queue-location').value == "" || document.getElementById('queue-location').value == "{{queuePos.location}}"){
    
        var hostname = document.getElementById('php_container').textContent.trim();
        var exclude = /share|kthopen|eduroam/;
        var include = /(csc|ug)\.kth\.se/;
        var computername = "";

        //Filters out the most common non-school computers
        if(exclude.test(hostname)){
            return;
        }
        //If it's a school computer, we get the name
        else if(include.test(hostname)){
            computername = capitaliseFirstLetter(insertSpace(splitAtDot(hostname)));
        }

        if(computername != ""){
            document.getElementById('queue-location').value = computername;
        }
    }
    else{
        return;
    }
}

