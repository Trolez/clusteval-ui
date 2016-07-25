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
    $('form.run-creation [data-mode]').find('input[type=hidden].placeholder').prop('disabled', false);

    //If a mode is already selected on page load, show appropriate sections
    var mode = $('form.run-creation input[type=radio][name=mode]:checked').val();
    if (mode != null && mode != "") {
        var elementsToShow = $('form.run-creation .filter').filter(function(){
            return $.inArray(mode.toString(), $(this).data('mode')) >= 0
        });

        elementsToShow.show();

        elementsToShow.find('input').prop('disabled', false);
        elementsToShow.find('select').prop('disabled', false);
        elementsToShow.find('input[type=hidden].placeholder').prop('disabled', true);
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
        elementsToHide.find('input[type=hidden].placeholder').prop('disabled', false);

        elementsToShow.find('input').prop('disabled', false);
        elementsToShow.find('select').prop('disabled', false);
        elementsToShow.find('input[type=hidden].placeholder').prop('disabled', true);

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
                    returnValue += '<input type="hidden" id="randomizers' + index + '.parameters' + paramIndex + '.description" name="randomizers[' + index + '].parameters[' + paramIndex + '].description" value="' + value.description + '" />'
                    returnValue += '<input type="hidden" id="randomizers' + index + '.parameters' + paramIndex + '.name" name="randomizers[' + index + '].parameters[' + paramIndex + '].name" value="' + value.name + '" />'
                    paramIndex++;
                });
                returnValue += '</div></div>';
                $(".randomizers").append(returnValue);
            }
        });
    });

    //Add program
    var val = $('form.run-creation select.programs').val();
    if (val == null) {
        val = [];
    }
    $('form.run-creation').on('change', 'select.programs', function() {
        var newVal = $(this).val();
        var selectedProgram;
        var index = $('form.run-creation #program-parameters .program').length;

        if (newVal == null) {
            //All have been removed - reset everything
            $('form.run-creation #program-parameters').empty();
            val = [];
        } else {
            //Check if element was selected
            for (var i = 0; i < newVal.length; i++) {
                if ($.inArray(newVal[i], val) == -1) {
                    selectedProgram = $(this).find('option[value="' + newVal[i] + '"]').text();
                    addProgramParameters(selectedProgram, index);
                }
            }

            //Check if element was unselected
            for (var i = 0; i < val.length; i++) {
                if ($.inArray(val[i], newVal) == -1) {
                    selectedProgram = $(this).find('option[value="' + val[i] + '"]').text();
                    $('form.run-creation #program-parameters .program[data-name=' + selectedProgram + ']').remove();
                }
            }

            val = newVal;
        }
    });

    function addProgramParameters(program, index) {
        $.ajax({
            url: "/getProgram?name=" + program,
            type: 'get',
            dataType: 'html',
            success: function(data) {
                result = $.parseJSON(data);

                var returnValue = '<div class="program" data-name="' + result.name + '">';
                    returnValue += '<span class="name"><strong>Program: </strong>' + result.name + '</span>';
                    returnValue += '<input type="hidden" id="programSettings' + index + '.name"' + 'name="programSettings[' + index + '].name" value="' + result.name + '" />';
                    returnValue += '<span class="toggle"><i class="fa fa-expand"></i></span>'
                    returnValue += '<div class="program-content">';
                        var paramIndex = 0;
                        $.each(result.parameters, function(key,value) {
                            var optionIndex = 0;
                            var defaultValue = '';
                            returnValue += '<hr><label>' + value.name + '</label><br>';
                            returnValue += '<input type="hidden" id="programSettings' + index + '.parameters' + paramIndex + '.name"' + 'name="programSettings[' + index + '].parameters[' + paramIndex + '].name" value="' + value.name + '" />';
                            $.each(value.options, function(key,value){
                                returnValue += value.name + ': ' + value.value + '<br>';
                                if (value.name == "defaultValue") {
                                    defaultValue = value.value;
                                }
                                returnValue += '<input type="hidden" id="programSettings' + index + '.parameters' + paramIndex + '.options' + optionIndex + '.name"' + 'name="programSettings[' + index + '].parameters[' + paramIndex + '].options[' + optionIndex + '].name" value="' + value.name + '" />';
                                returnValue += '<input type="hidden" id="programSettings' + index + '.parameters' + paramIndex + '.options' + optionIndex + '.value"' + 'name="programSettings[' + index + '].parameters[' + paramIndex + '].options[' + optionIndex + '].value" value="' + value.value + '" />';
                                optionIndex++;
                            });

                            if (value.optimizable) {
                                if ($('form.run-creation input[name=mode]:checked').val() != "parameter_optimization") {
                                    returnValue += '<div class="filter" data-mode="[&quot;parameter_optimization&quot;]" style="display: none;"><div class="checkbox">';
                                } else {
                                    returnValue += '<div class="filter" data-mode="[&quot;parameter_optimization&quot;]"><div class="checkbox">';
                                }
                                        returnValue += '<label>';
                                            returnValue += '<input type="checkbox" checked value="true" id="programSettings' + index + '.parameters' + paramIndex + '.optimize1" name="programSettings[' + index + '].parameters[' + paramIndex + '].optimize">Optimize';
                                        returnValue += '</label>';
                                        returnValue += '<input type="hidden" id="programSettings' + index + '.parameters' + paramIndex + '.optimizable" name="programSettings[' + index + '].parameters[' + paramIndex + '].optimizable" value="true">';
                                        returnValue += '<input name="_programSettings[' + index + '].parameters[' + paramIndex + '].optimize" value="on" type="hidden">';
                                    returnValue += '</div></div>';
                            }

                            returnValue += '<input class="form-control" id="programSettings' + index + '.parameters' + paramIndex + '.value" name="programSettings[' + index + '].parameters[' + paramIndex + '].value" type="text" value="' + defaultValue + '" />';
                            paramIndex++;
                        });
                    returnValue += '</div>';
                returnValue += '</div>';
                $("#program-parameters").append(returnValue);
            }
        });
    }

    //Toogle program parameter collapse
    $('form.run-creation #program-parameters').on('click', '.toggle', function() {
        $(this).parent().find('.program-content').slideToggle();
        $(this).find('.fa').toggleClass('fa-expand');
        $(this).find('.fa').toggleClass('fa-compress');
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
