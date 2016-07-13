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
        elementsToShow.find('input[type=hidden].enabled').prop('disabled', false);
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
        elementsToShow.find('input[type=hidden].enabled').prop('disabled', false);

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

    //Add randomizer
    $('form.run-creation .add-randomizer').click(function() {
        var index = $('form.run-creation .randomizers .randomizer').length;
        var randomizer = $(this).data('randomizer');

        $.ajax({
            url: "/getRandomizer?name=" + randomizer,
            type: 'get',
            dataType: 'html',
            success: function(data) {
                result = $.parseJSON(data);

                var returnValue = '<div class="randomizer"><div class="form-group">';
                var paramIndex = 0;
                $.each(result.parameters, function(key,value) {
                    returnValue += '<label>' + value.name + ' - ' + value.description + '</label>';
                    returnValue += '<input class="form-control" type="text" id="randomizers' + index + '.parameters' + paramIndex + '.value" name="randomizers[' + index + '].parameters[' + paramIndex + '].value" />'
                    returnValue += '<input class="enabled" type="hidden" id="randomizers' + index + '.parameters' + paramIndex + '.description" name="randomizers[' + index + '].parameters[' + paramIndex + '].description" value="' + value.description + '" />'
                    returnValue += '<input class="enabled" type="hidden" id="randomizers' + index + '.parameters' + paramIndex + '.name" name="randomizers[' + index + '].parameters[' + paramIndex + '].name" value="' + value.name + '" />'
                    paramIndex++;
                });
                returnValue += '</div></div>';
                $(".randomizers").append(returnValue);
            }
        });
    });

    //Add program
    var val;
    $('form.run-creation').on('change', 'select.programs', function() {
        var newVal = $(this).val();
        var selectedProgram;
        var index = $('form.run-creation #program-parameters .program').length;
        for (var i = 0; i < newVal.length; i++) {
            if ($.inArray(newVal[i], val) == -1) {
                selectedProgram = $(this).find('option[value="' + newVal[i] + '"]').text();
                $.ajax({
                    url: "/getProgram?name=" + selectedProgram,
                    type: 'get',
                    dataType: 'html',
                    success: function(data) {
                        result = $.parseJSON(data);

                        var returnValue = '<div class="program"><div class="form-group">';
                        returnValue += '<strong>Program: </strong>' + result.name;
                        //var paramIndex = 0;
                        $.each(result.parameters, function(key,value) {
                            returnValue += '<hr><label>' + value.name + '</label><br>';
                            $.each(value.options, function(key,value){
                                returnValue += value.name;
                                returnValue += '<input class="form-control" type="text" value="' + value.value + '" />';
                            });
                            //returnValue += '<input class="enabled" type="hidden" id="randomizers' + index + '.parameters' + paramIndex + '.description" name="randomizers[' + index + '].parameters[' + paramIndex + '].description" value="' + value.description + '" />'
                            //returnValue += '<input class="enabled" type="hidden" id="randomizers' + index + '.parameters' + paramIndex + '.name" name="randomizers[' + index + '].parameters[' + paramIndex + '].name" value="' + value.name + '" />'
                            //paramIndex++;
                        });
                        returnValue += '</div></div>';
                        $("#program-parameters").append(returnValue);
                    }
                });
            }
        }
        val = newVal;
    });

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

    $('.run-list .delete-run').click(function() {
        return confirm("Delete this run?");
    });
});
