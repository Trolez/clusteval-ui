$(document).ready(function() {
    /************************
    *** Run creation form ***
    ************************/

    //Add select2 to multiple select boxes
    $('.select2-multiple').select2({
        closeOnSelect: false,
        width: '100%'
    });

    //Make all mode-specific sections initially invisible
    $('form.run-creation [data-mode]').hide();
    $('form.run-creation [data-mode]').find('input,select').prop('disabled', true);
    $('form.run-creation [data-mode]').find('input[type=hidden].placeholder').prop('disabled', false);

    //If a mode is already selected on page load, show appropriate sections
    if ($('form.run-creation input[type=radio][name=mode]:checked').length > 0) {
        updateMode($('form.run-creation input[type=radio][name=mode]:checked'), $('form.run-creation'));
    }

    //Mode select/change
    $('form.run-creation input[type=radio][name=mode]').change(function() {
        updateMode($(this), $('form.run-creation'));
        updateAccordion();
    });

    function updateMode(radio, parent) {
        var mode = $(radio).val();

        //Only show mode-specific sections for the selected mode
        var elementsToHide = parent.find('.filter').filter(function(){
            return $.inArray(mode.toString(), $(this).data('mode')) < 0
        });

        var elementsToShow = parent.find('.filter').filter(function(){
            return $.inArray(mode.toString(), $(this).data('mode')) >= 0
        });

        elementsToShow.show();
        elementsToShow.find('input,select').prop('disabled', false);
        elementsToShow.find('input[type=hidden].placeholder').prop('disabled', true);

        elementsToHide.hide();
        elementsToHide.find('input,select').prop('disabled', true);
        elementsToHide.find('input[type=hidden].placeholder').prop('disabled', false);

        if (mode == "parameter_optimization") {
            $('.optimize-parameter').find('input').prop('disabled', false);
            $('.optimize-parameter.hidden').find('input').prop('disabled', true);
        }
    }

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
                            var minValue = '';
                            var maxValue = '';
                            var options = '';

                            returnValue += '<div class="program-content-parameter">';
                                returnValue += '<hr><label>' + value.name + '</label><br>';
                                returnValue += '<input type="hidden" id="programSettings' + index + '.parameters' + paramIndex + '.name"' + 'name="programSettings[' + index + '].parameters[' + paramIndex + '].name" value="' + value.name + '" />';
                                $.each(value.defaultOptions, function(key,value){
                                    returnValue += value.name + ': ' + value.value + '<br>';
                                    if (value.name == "defaultValue") {
                                        defaultValue = value.value;
                                    }
                                    if (value.name == "minValue") {
                                        minValue = value.value;
                                    }
                                    if (value.name == "maxValue") {
                                        maxValue = value.value;
                                    }
                                    if (value.name == "options") {
                                        options = value.value;
                                    }
                                    returnValue += '<input type="hidden" id="programSettings' + index + '.parameters' + paramIndex + '.defaultOptions' + optionIndex + '.name"' + 'name="programSettings[' + index + '].parameters[' + paramIndex + '].defaultOptions[' + optionIndex + '].name" value="' + value.name + '" />';
                                    returnValue += '<input type="hidden" id="programSettings' + index + '.parameters' + paramIndex + '.defaultOptions' + optionIndex + '.value"' + 'name="programSettings[' + index + '].parameters[' + paramIndex + '].defaultOptions[' + optionIndex + '].value" value="' + value.value + '" />';
                                    optionIndex++;
                                });

                                returnValue += '<div class="filter" data-mode="[&quot;parameter_optimization&quot;]">';
                                    if (value.optimizable) {
                                        returnValue += '<div class="checkbox">';
                                            returnValue += '<label>';
                                                returnValue += '<input class="checkbox-optimize" type="checkbox" checked value="true" id="programSettings' + index + '.parameters' + paramIndex + '.optimize1" name="programSettings[' + index + '].parameters[' + paramIndex + '].optimize">Optimize';
                                            returnValue += '</label>';
                                            returnValue += '<input type="hidden" id="programSettings' + index + '.parameters' + paramIndex + '.optimizable" name="programSettings[' + index + '].parameters[' + paramIndex + '].optimizable" value="true">';
                                            returnValue += '<input name="_programSettings[' + index + '].parameters[' + paramIndex + '].optimize" value="on" type="hidden">';
                                        returnValue += '</div>';
                                    }
                                    if (options == '') {
                                        if (value.optimizable) {
                                            returnValue += '<div class="optimize-parameter optimize">';
                                                returnValue += '<label>Min value';
                                                    returnValue += '<input class="form-control" id="programSettings' + index + '.parameters' + paramIndex + '.minValue" name="programSettings[' + index + '].parameters[' + paramIndex + '].minValue" type="text" value="' + minValue + '" />';
                                                returnValue += '</label>';
                                                returnValue += '<label>Max value';
                                                    returnValue += '<input class="form-control" id="programSettings' + index + '.parameters' + paramIndex + '.maxValue" name="programSettings[' + index + '].parameters[' + paramIndex + '].maxValue" type="text" value="' + maxValue + '" />';
                                                returnValue += '</label>';
                                            returnValue += '</div>';
                                            returnValue += '<div class="optimize-parameter no-optimize hidden" style="display: none;">';
                                                returnValue += '<label>Value';
                                                    returnValue += '<input class="form-control" id="programSettings' + index + '.parameters' + paramIndex + '.value" name="programSettings[' + index + '].parameters[' + paramIndex + '].value" type="text" value="' + defaultValue + '" disabled />';
                                                returnValue += '</label>';
                                            returnValue += '</div>';
                                        } else {
                                            returnValue += '<div>';
                                                returnValue += '<label>Value';
                                                    returnValue += '<input class="form-control" id="programSettings' + index + '.parameters' + paramIndex + '.value" name="programSettings[' + index + '].parameters[' + paramIndex + '].value" type="text" value="' + defaultValue + '" />';
                                                returnValue += '</label>';
                                            returnValue += '</div>';
                                        }
                                    } else {
                                        returnValue += '<label>Values';
                                            returnValue += '<input class="form-control" id="programSettings' + index + '.parameters' + paramIndex + '.options" name="programSettings[' + index + '].parameters[' + paramIndex + '].options" type="text" value="' + options + '" />';
                                        returnValue += '</label>';
                                    }
                                returnValue += '</div>';

                                returnValue += '<div class="filter" data-mode="[&quot;clustering&quot;,&quot;robustnessAnalysis&quot;]">';
                                    returnValue += '<label>Value';
                                        returnValue += '<input class="form-control" id="programSettings' + index + '.parameters' + paramIndex + '.value" name="programSettings[' + index + '].parameters[' + paramIndex + '].value" type="text" value="' + defaultValue + '" />';
                                    returnValue += '</label>';
                                returnValue += '</div>';
                            returnValue += '</div>';

                            paramIndex++;
                        });
                    returnValue += '</div>';
                returnValue += '</div>';
                $("#program-parameters").append(returnValue);
                updateMode($('form.run-creation input[type=radio][name=mode]:checked'), $('form.run-creation #program-parameters'));
            }
        });
    }

    //Toogle program parameter collapse
    $('form.run-creation #program-parameters').on('click', '.toggle', function() {
        $(this).parent().find('.program-content').slideToggle();
        $(this).find('.fa').toggleClass('fa-expand');
        $(this).find('.fa').toggleClass('fa-compress');
    });

    //Toggle parameter optimization
    $('form.run-creation #program-parameters').on('change', 'input.checkbox-optimize', function() {
        var content = $(this).closest('.program-content-parameter');
        if ($(this).is(':checked')) {
            content.find('.optimize-parameter.optimize').show();
            content.find('.optimize-parameter.optimize').find('input').prop('disabled', false);
            content.find('.optimize-parameter.optimize').removeClass('hidden');

            content.find('.optimize-parameter.no-optimize').hide();
            content.find('.optimize-parameter.no-optimize').find('input').prop('disabled', true);
            content.find('.optimize-parameter.no-optimize').addClass('hidden');
        } else {
            content.find('.optimize-parameter.optimize').hide();
            content.find('.optimize-parameter.optimize').find('input').prop('disabled', true);
            content.find('.optimize-parameter.optimize').addClass('hidden');

            content.find('.optimize-parameter.no-optimize').show();
            content.find('.optimize-parameter.no-optimize').find('input').prop('disabled', false);
            content.find('.optimize-parameter.no-optimize').removeClass('hidden');
        }
    });

    //Trigger parameter optimize checkbox on page load
    $('form.run-creation #program-parameters input.checkbox-optimize').trigger('change');

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

    //Function that updates the progress bars of runs that are in progress
    function updateRunProgress() {
        $.ajax({
            url: "/getRunStatus",
            type: 'get',
            dataType: 'html',
            success: function(data) {
                result = $.parseJSON(data);

                $.each(result, function (runName, status) {
                    var progressBar = $('.run-list .run[data-name=' + runName + '] .progress-bar');
                    progressBar.attr('aria-valuenow', status.second);
                    progressBar.css('width', status.second + '%');
                    progressBar.find('.sr-only').text(status.second + '% Complete');
                });
            }
        });

        //Update every 10 seconds
        setTimeout(updateRunProgress, 10000);
    }
    updateRunProgress();

    //Fetch information about data config file on click
    $('.data-container .toggle').click(function() {
        var name = $(this).parent().find('.fetch-data').html();
        var container = $(this).parent().find('.data-information');

        $(this).toggleClass('open');
        $(this).find('.fa').toggleClass('fa-expand');
        $(this).find('.fa').toggleClass('fa-compress');

        if (container.is(':visible')) {
            container.slideUp();
        } else {
            $.ajax({
                url: "/data/show?name=" + name,
                type: 'get',
                dataType: 'html',
                success: function(data) {
                    container.html(data);
                    container.slideDown();
                }
            });
        }
    });

    $('#data .expand-all').click(function() {
        $('.data-container .toggle:not(.open)').trigger('click');
    });

    $('#data .collapse-all').click(function() {
        $('.data-container .toggle.open').trigger('click');
    });

    //Fetch information about program config file on click
    $('.program-container .toggle').click(function() {
        var name = $(this).parent().find('.fetch-program').html();
        var container = $(this).parent().find('.program-information');

        $(this).toggleClass('open');
        $(this).find('.fa').toggleClass('fa-expand');
        $(this).find('.fa').toggleClass('fa-compress');

        if (container.is(':visible')) {
            container.slideUp();
        } else {
            $.ajax({
                url: "/programs/show?name=" + name,
                type: 'get',
                dataType: 'html',
                success: function(data) {
                    container.html(data);
                    container.slideDown();
                }
            });
        }
    });

    $('#programs .expand-all').click(function() {
        $('.program-container .toggle:not(.open)').trigger('click');
    });

    $('#programs .collapse-all').click(function() {
        $('.program-container .toggle.open').trigger('click');
    });

    //Toggle result graphs
    $('.toggle-result-graphs').click(function() {
        $(this).parent().find('.result-graphs').slideToggle();
        $(this).find('.fa').toggleClass('fa-chevron-up');
        $(this).find('.fa').toggleClass('fa-chevron-down');
    });

    //Show value of range input
    $('.parameter-sliders, .cluster-parameter-sliders').on('input change', '.range-container input[type=range]', function() {
        if ($(this).hasClass('options')) {
            try {
                var options = $(this).data('options').split(',');
                $(this).next('.range-value').html(options[$(this).val()]);
            }
            catch (err) {
                //Fix in case there is only one option
                var options = $(this).data('options');
                $(this).next('.range-value').html(options);
            }
        } else {
            $(this).next('.range-value').html($(this).val());
        }
    });

    //Change active radio on parameter slider
    $('.parameter-sliders').on('change', '.parameter-graph-form input[type=radio]', function() {
        $('.parameter-graph-form').find('.icon .locked').show();
        $('.parameter-graph-form').find('.icon .unlocked').hide();
        $('.parameter-graph-form').find('.range-input-container').show();
        $('.parameter-graph-form').find('.range-input-locked').hide();
        if ($(this).is(':checked')) {
            $(this).closest('.range-container').find('.icon .locked').hide();
            $(this).closest('.range-container').find('.icon .unlocked').show();
            $(this).parent().find('.range-input-container').hide();
            $(this).parent().find('.range-input-locked').show();
        }
    });

    //Parameter graph form submit
    $('.parameter-sliders').on('submit', '.parameter-graph-form', function(e) {
        e.preventDefault();

        var activeParameter = $(this).find('input[type=radio]:checked').val();

        var parameters = [];

        $(this).find('input[type=radio]:not(:checked)').each(function(n) {
            var value = $(this).parent().find('.range-value').html();
            parameters[n] = $(this).val() + "=" + value;
        });

        var name = $(this).find('input[name=name]').val();
        var program = $(this).find('input[name=program]').val();
        var data = $(this).find('input[name=data]').val();

        var graphContainer = $(this).closest('.parameter-sliders').find('.parameter-graph');

        $.get("/results/get-parameter-graph?active-parameter=" + activeParameter + "&parameters=" + parameters.join(',') + "&name=" + name + "&program=" + program + "&data=" + data, function(csv) {
            var allTextLines = csv.split('\n');
            var header = allTextLines[1].split(',');
            var graphType = 'column';
            if ($.isNumeric(header[0])) {
                graphType = 'line';
            }

            //alert(csv);
            graphContainer.highcharts({
                chart: {
                    type: graphType,
                    zoomType: 'x'
                },
                plotOptions: {
                    series: {
                        step: 'left'
                    }
                },
                data: {
                    csv: csv,
                    itemDelimiter: ",",
                    lineDelimiter: "\n"
                },
                title: {
                    text: 'Qualities'
                },
                yAxis: {
                    title: {
                        text: 'Value'
                    }
                }
            });
        });
    });

    //See clustering - modal
    $('.cluster-modal').click(function(event) {
        event.preventDefault();
        $.get(this.href, function(html) {
            $(html).appendTo('body').modal();
        });
    });

    //Automatically submit graph form on change
    $('.parameter-sliders').on('change', 'input', function (e) {
        var form = $(this).closest('form');
        if (form.find('input[type=radio]:checked').length >= 1) {
            $(this).closest('form').submit();
        }
    });

    //Load in all sliders on result page
    $('.parameter-optimization .parameter-sliders').each(function() {
        var container = $(this);
        var name = $(this).data('name');
        var program = $(this).data('program');
        var data = $(this).data('data');
        $.ajax({
            url: "/results/get-parameter-sliders?name=" + name + "&program=" + program + "&data=" + data,
            type: 'get',
            dataType: 'html',
            success: function(data) {
                container.html(data);

                //Trigger a change, so the div containing the value is initialized
                container.find('input[type=range]').trigger('change');
            }
        });
    });

    //Load in clustering
    $('#load-clustering, .load-clustering').each(function() {
        var container = $(this);
        var name = $(this).data('name');
        var program = $(this).data('program');
        var data = $(this).data('data');
        var paramset = $(this).data('paramset');

        var clusterUrl = "";
        if (paramset == null) {
            clusterUrl = "/results/load-clustering-cluster?name=" + name + "&program=" + program + "&data=" + data;
        } else {
            clusterUrl = "/results/load-clustering?name=" + name + "&program=" + program + "&data=" + data + "&param-set=" + paramset;
        }

        $.ajax({
            url: clusterUrl,
            type: 'get',
            dataType: 'html',
            success: function(data) {
                container.html(data);
            }
        });
    });

    //Load in all sliders on clustering page
    $('#clustering .cluster-parameter-sliders').each(function() {
        var container = $(this);
        var name = $(this).data('name');
        var program = $(this).data('program');
        var data = $(this).data('data');
        $.ajax({
            url: "/results/get-clustering-sliders?name=" + name + "&program=" + program + "&data=" + data,
            type: 'get',
            dataType: 'html',
            success: function(data) {
                container.html(data);

                //Set values of sliders to what is in the url
                var urlValues = getParameterByName('param-set');
                var parameters = urlValues.split(',');

                for (var i = 0; i < parameters.length; i++) {
                    var parts = parameters[i].split('=');
                    $('input[value=' + parts[0] + ']').each(function() {
                        var valueArray = $(this).parent().find('input[type=range]').data('options').split(',');
                        var index = valueArray.indexOf(parts[1]);
                        $('input[value=' + parts[0] + ']').parent().find('input[type=range]').val(index);
                        $('input[value=' + parts[0] + ']').parent().find('.range-value').html(parts[1]);
                    })
                };
            }
        });
    });

    //Automatically submit graph form on change
    $('.cluster-parameter-sliders').on('change', 'input', function (e) {
        $(this).closest('form').find('.range-value').each(function() {
            if ($(this).html() == '') {
                return;
            }
        });
        var form = $(this).closest('form');
        $(this).closest('form').submit();
    });

    //Clustering graph form submit
    $('.cluster-parameter-sliders').on('submit', '.parameter-cluster-form', function(e) {
        e.preventDefault();

        //Load in clustering
        var container = $('#load-clustering');
        var name = container.data('name');
        var program = container.data('program');
        var data = container.data('data');
        var paramset = container.data('paramset');

        var parameters = [];

        $(this).find('.range-value').each(function(n) {
            var parameterName = $(this).parent().find('input[type=hidden]').val();
            var value = $(this).html();
            parameters[n] = parameterName + "=" + value;
        });

        $.ajax({
            url: "/results/load-clustering?name=" + name + "&program=" + program + "&data=" + data + "&param-set=" + parameters.join(','),
            type: 'get',
            dataType: 'html',
            success: function(data) {
                container.html(data);
            }
        });
    });

    function getParameterByName(name, url) {
        if (!url) url = window.location.href;
        name = name.replace(/[\[\]]/g, "\\$&");
        var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
            results = regex.exec(url);
        if (!results) return null;
        if (!results[2]) return '';
        return decodeURIComponent(results[2].replace(/\+/g, " "));
    }
});
