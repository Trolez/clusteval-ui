$(document).ready(function() {
    /****************
    *** Accordion ***
    ****************/

    var subtract = ($('.myAccordion > li').length - 1) * 75;
    $('.myAccordion > li:first-child').css('width', 'calc(100% - ' + subtract + 'px)');

    var width = $('.myAccordion > li:first-child').width() - 75;
    $('.myAccordion .section-content').css('min-width', width);

    $('.myAccordion > li').addClass('animated');

    $('.myAccordion > li').click(function (){
            $('.myAccordion > li').css('width', '75px');

            var subtract = ($('.myAccordion > li').length - 1) * 75;
            $(this).css('width', 'calc(100% - ' + subtract + 'px)');
            $(this).find('.section-content').css('width', 'calc(100% - ' + subtract + 'px)');
    });

    /****************
    ****************/

    //Add select2 to multiple select boxes
    $('.select2-multiple').select2({
        width: '100%'
    });

    /************************
    *** Run creation form ***
    ************************/

    //Make all mode-specific sections initially invisible
    $('form.run-creation [data-mode]').hide();

    $('form.run-creation input[type=radio][name=mode]').change(function(){
        var mode = $(this).val();

        //Only show mode-specific sections for the selected mode
        /*$('form.run-creation [data-mode]').hide();
        $('form.run-creation [data-mode=' + mode + ']').show();*/

        $('form.run-creation .filter').filter(function(){
            return $.inArray(mode.toString(), $(this).data('mode')) < 0
        }).hide();
        $('form.run-creation .filter').filter(function(){
            return $.inArray(mode.toString(), $(this).data('mode')) >= 0
        }).show();
    });

    /****************
    ****************/
});
