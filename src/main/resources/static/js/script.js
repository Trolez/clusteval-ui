$(document).ready(function() {
    /************************
    *** Run creation form ***
    ************************/

    //Add select2 to multiple select boxes
    $('.select2-multiple').select2({
        closeOnSelect: false,
        width: '100%'
    });
    /*$('.select2-multiple').selectize({
        maxOptions: 100
    });*/

    //Make all mode-specific sections initially invisible
    $('form.run-creation [data-mode]').hide();
    $('form.run-creation [data-mode]').find('input').prop('disabled', true);
    $('form.run-creation [data-mode]').find('select').prop('disabled', true);
    $('form.run-creation [data-mode]').find('input[type=hidden]').prop('disabled', false);

    //If a mode is already selected on page load, show appropriate sections
    var mode = $('form.run-creation input[type=radio][name=mode]:checked').val();
    if (mode != null && mode != "") {
        var elementsToShow = $('form.run-creation .filter').filter(function(){
            return $.inArray(mode.toString(), $(this).data('mode')) >= 0
        });

        elementsToShow.show();

        elementsToShow.find('input').prop('disabled', false);
        elementsToShow.find('select').prop('disabled', false);
        elementsToShow.find('input[type=hidden]').prop('disabled', true);
    }

    //Mode select/change
    $('form.run-creation input[type=radio][name=mode]').change(function(){
        var mode = $(this).val();

        //Only show mode-specific sections for the selected mode
        var elementsToHide = $('form.run-creation .filter').filter(function(){
            return $.inArray(mode.toString(), $(this).data('mode')) < 0
        });

        var elementsToShow = $('form.run-creation .filter').filter(function(){
            return $.inArray(mode.toString(), $(this).data('mode')) >= 0
        });

        elementsToHide.hide();
        elementsToShow.show();

        elementsToHide.find('input').prop('disabled', true);
        elementsToHide.find('select').prop('disabled', true);
        elementsToHide.find('input[type=hidden]').prop('disabled', false);

        elementsToShow.find('input').prop('disabled', false);
        elementsToShow.find('select').prop('disabled', false);
        elementsToShow.find('input[type=hidden]').prop('disabled', true);

        updateAccordion();
    });

    //When selecting a quality measure, enable the corresponding optimization criterion
    $('form.run-creation select[name=qualityMeasures]').change(function(){
        updateOptimizationCriterion($(this));
    });
    function updateOptimizationCriterion(element) {
        $('form.run-creation select[name=optimizationCriterion] option').attr('disabled', 'disabled');
        $(element).find('option:selected').each(function(){
            var value = $(this).val();
            $('form.run-creation select[name=optimizationCriterion]').find('option[value=' + value + ']').removeAttr('disabled');
        });

        //Reinitialize select2 to reflect changes
        $('form.run-creation select[name=optimizationCriterion]').select2({ closeOnSelect: false, width: '100%' });
    }

    //Initially update criterion
    updateOptimizationCriterion('form.run-creation select[name=qualityMeasures]');

    

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

    function updateAccordion() {
        $('.myAccordion > li').show();
        hideElementWithNoVisibleChildren('.myAccordion > li');

        $('.myAccordion > li').removeClass('animated');

        var subtract = ($('.myAccordion > li:visible').length - 1) * 75;
        $('.myAccordion > li:first-child').css('width', 'calc(100% - ' + subtract + 'px)');

        var width = $('.myAccordion > li:first-child').width() - 75;
        $('.myAccordion .section-content').css('min-width', width);

        $('.myAccordion > li').addClass('animated');
    }

    //Initially hide appropriate slides
    hideElementWithNoVisibleChildren('.myAccordion > li');

    //Add error class to any tabs that contains error
    $('.myAccordion > li').filter(function() {
        return $(this).find('span.error').length > 0;
    }).find('.section-header').addClass('error');

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
