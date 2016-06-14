$(document).ready(function() {
    /************************
    *** Run creation form ***
    ************************/

    //Add select2 to multiple select boxes
    $('.select2-multiple').select2({
        width: '100%'
    });

    //Make all mode-specific sections initially invisible
    $('form.run-creation [data-mode]').hide();

    //If a mode is already selected on page load, show appropriate sections
    var mode = $('form.run-creation input[type=radio][name=mode]:checked').val();
    if (mode != null && mode != "") {
        $('form.run-creation .filter').filter(function(){
            return $.inArray(mode.toString(), $(this).data('mode')) >= 0
        }).show();
    }

    //Mode select/change
    $('form.run-creation input[type=radio][name=mode]').change(function(){
        var mode = $(this).val();

        //Only show mode-specific sections for the selected mode
        $('form.run-creation .filter').filter(function(){
            return $.inArray(mode.toString(), $(this).data('mode')) < 0
        }).hide();
        $('form.run-creation .filter').filter(function(){
            return $.inArray(mode.toString(), $(this).data('mode')) >= 0
        }).show();
        updateAccordion();
    });

    /****************
    ****************/

    /****************
    *** Accordion ***
    ****************/
    function hideElementWithNoVisibleChildren(elements) {
        //Hide all empty slides
        $(elements).filter(function() {
            return $(this).find('.section-content').children('div:visible').length == 0;
        }).hide();
    }

    function showElementWithVisibleChildren(elements) {
        //Show non-empty slides
        $(elements).filter(function() {
            return $(this).find('.section-content').children('div:visible').length > 0;
        }).show();
    }

    function updateAccordion() {
        $('.myAccordion > li').show();
        hideElementWithNoVisibleChildren('.myAccordion > li');
        //showElementWithVisibleChildren('.myAccordion > li');

        $('.myAccordion > li').removeClass('animated');

        var subtract = ($('.myAccordion > li:visible').length - 1) * 75;
        $('.myAccordion > li:first-child').css('width', 'calc(100% - ' + subtract + 'px)');

        var width = $('.myAccordion > li:first-child').width() - 75;
        $('.myAccordion .section-content').css('min-width', width);

        $('.myAccordion > li').addClass('animated');
    }

    //Initially hide appropriate slides
    hideElementWithNoVisibleChildren('.myAccordion > li');

    var subtract = ($('.myAccordion > li:visible').length - 1) * 75;
    $('.myAccordion > li:first-child').css('width', 'calc(100% - ' + subtract + 'px)');

    var width = $('.myAccordion > li:first-child').width() - 75;
    $('.myAccordion .section-content').css('min-width', width);

    $('.myAccordion > li').addClass('animated');

    $('.myAccordion > li').click(function (){
            $('.myAccordion > li').css('width', '75px');

            var subtract = ($('.myAccordion > li:visible').length - 1) * 75;
            $(this).css('width', 'calc(100% - ' + subtract + 'px)');
            $(this).find('.section-content').css('width', 'calc(100% - ' + subtract + 'px)');
    });

    /****************
    ****************/
});
