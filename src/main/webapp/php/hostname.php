<?php	
	$hostname = htmlspecialchars(gethostbyaddr($_SERVER['REMOTE_ADDR']));
	echo $hostname;
?>

