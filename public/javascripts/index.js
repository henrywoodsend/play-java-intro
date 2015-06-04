
$(document).ready(function() {

    /**
	 * On Search Input Key Up try search, or navigate through search results (cursor keys)
	 */
	$('.search').keyup(function(e) {

		 /*Cursor Keys navigate through results drop down, enter to update results*/
	    if (e.keyCode == 38 || e.keyCode == 40 || e.keyCode == 13) {

	        var $results = $('.searchresultlist li');

	        var $current = $results.filter('.selected'),$next;

	        switch ( e.keyCode ) {
	            case 38: // Up
	                $next = $current.prev();

	                break;
	            case 40: // Down
	                if (!$results.hasClass('selected')) {
	                       $results.first().addClass('selected');
	                }
	                $next = $current.next();
	                break;
	            case 13: // Enter

	            	var type = $('[id*=displaytype]').text();
	            	var dt="";
	            	if (type=="by vintage")
	            		dt = "t=bv&";

	                if ($results.hasClass('selected')) {
	                	window.location.href = $current.find('a').attr('href');
	                	return false;
	                }
	                break;
	        }

	        //only check next element if up and down key pressed
	        if ( $next.is('li') ) {
	            $current.removeClass('selected');
	            $next.addClass('selected');
	        }

	        //update text in searchbar
	        if ($results.hasClass('selected')) {
	            $('.search').val($('.selected').text());
	        }

	        //set cursor position
	        if(e.keyCode === 38) return false;

	        return;

	    /*Otherwise try search if not escape character*/
	    } else if (e.keyCode != 27){
	    	trysearch(this.value);
	    }
	});
});

/**
 * Called after every keydown event of search input
 * Only perform search after set time delay
 * If, after time delay, further calls (ie further key presses) have been made, prior calls
 * will be ignored.  This is done using the checkcount var, only the call with the most recent check count actually
 * performs the validation
 */
var checkcount=0;
function trysearch(searchstring){

	checkcount++;
	var thiscount=checkcount;
	setTimeout(function() {
		if (thiscount==checkcount){
			winesearch(searchstring);
		}
	}, 100);
}

function winesearch(searchstring){
	if (searchstring.length>0){

		$.getJSON( "/wine-names?searchstring="+searchstring,function( winelist ) {

			var dropdown = $('.searchresultlist');

			dropdown.children('li').remove(); //Remove existing results

			for (var i=0; i < winelist.length; i++) {
				dropdown.append("<li onmouseover='searchresulthover(this)' wineid='"+winelist[i].id+"'><a href='wines/"+winelist[i].id+"'>"+winelist[i].name+"</a></li>");
			}
			dropdown.show();
        });
	}
}

/**
 * On hover over search result item, add 'selected' class
 * (required here rather than css as cursor can also move through search items)
 */
function searchresulthover(item){
	$(item).parent().children().removeClass('selected');
	$(item).addClass('selected');
}

/**
 * On click anywhere outside searchresultlist, hide searchresult list
 */
$( document ).on( 'click', function ( e ) {
    if ( $( e.target ).closest( '.searchresultlist' ).length === 0  && $(e.target).html()!='Search') {
        $( '.searchresultlist' ).hide();
    }
});
/**
 * On esc key press, hide searchresultlist
 */
$( document ).on( 'keydown', function ( e ) {
    if ( e.keyCode === 27 ) { // ESC
        $( '.searchresultlist' ).hide();
    }
});