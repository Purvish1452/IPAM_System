var formManager =
{
    // --------------------------------------------------------------------------kendo grid search filter------------------------------------------------------------------------------------------------//

    searchFilter : function (id)
    {
        $('#searchFilter').on('input', function (e)
        {
            var grid = id.data('kendoGrid');

            var columns = grid.columns;

            var filter =
                {
                    logic: 'or',
                    filters: []
                };

            columns.forEach(function (x)
            {
                if (x.field)
                {
                    var type = grid.options.schema.model.fields[x.field].type;
                    //var type = grid.dataSource.options.schema.model.fields[x.field].type;

                    if (type == 'string')
                    {
                        filter.filters.push({
                            field: x.field,
                            operator: 'contains',
                            value: e.target.value.trim()
                        })
                    }
                    else if (type == 'number')
                    {
                        if (formManager.isNumeric(e.target.value))
                        {
                            filter.filters.push({
                                field: x.field,
                                operator: 'eq',
                                value: e.target.value.trim()
                            });
                        }
                    }
                    else if (type == 'date')
                    {
                        var data = grid.dataSource.data();
                        for (var i=0;i<data.length ; i++)
                        {
                            var dateStr = kendo.format(x.format, data[i][x.field]);
                            if(dateStr.startsWith(e.target.value)){
                                filter.filters.push({
                                    field: x.field,
                                    operator:'eq',
                                    value: data[i][x.field].trim()
                                })
                            }
                        }
                    }
                    else if (type == 'boolean' && formManager.getBoolean(e.target.value) !== null)
                    {
                        var bool = formManager.getBoolean(e.target.value);
                        filter.filters.push({
                            field: x.field,
                            operator: 'eq',
                            value: bool
                        });
                    }
                }
            });
            grid.dataSource.filter(filter);
        });
    },

    isNumeric : function (n)
    {
        return !isNaN(parseFloat(n)) && isFinite(n);
    },

    getBoolean : function (str)
    {
        var ua = window.navigator.userAgent;
        var msie = ua.indexOf("MSIE ");

        // for IE search issue IPAM-109 (Startswith not supported in IE)
        if (msie > 0 || !!navigator.userAgent.match(/Trident.*rv\:11\./))
        {
            if("yes".indexOf(str.toLowerCase())>0){
                return true;
            } else if("no".indexOf(str.toLowerCase())>0){
                return false;
            } else {
                return null;
            }
        }
        else
        {
            if("yes".startsWith(str.toLowerCase())){
                return true;
            } else if("no".startsWith(str.toLowerCase())){
                return false;
            } else {
                return null;
            }
        }
    },

    // ----------------------------------------------------------------------Serialize form & get json data----------------------------------------------------------------------------------------------------//

    serializeForm: function (context)
    {
        var elements, elementSize, element;

        var elementType;

        var params = {};

        elements = context.find('input[type=text],textarea,input[type=password],select,select[data-type=multiple],input[type=email],input[type=file],input[type=number]');

        if (elements != undefined && elements.length > 0)
        {
            elementSize = elements.length;

            for (var elementIndex = 0; elementIndex < elementSize; elementIndex++)
            {
                element = $(elements[elementIndex]);

                if(element.context.dataset.type != null && element.context.dataset.type && element.context.dataset.type == 'multiple' && element.val() != null)
                {
                    params[element.attr('name')] = element.val().toString();
                }
                else
                {
                    params[element.attr('name')] = this.getElementValue(element, true);
                }

            }
        }

        elements = context.find('input[type=checkbox],input[type=radio]');

        elementType = elements.prop('type');

        if (elements != undefined && elements.length > 0)
        {
            elementSize = elements.length;

            for (var elementIndex = 0; elementIndex < elementSize; elementIndex++)
            {

                element = $(elements[elementIndex]);

                if (!element.prop('disabled') && element.prop('checked'))
                {
                    if (element.data('composite'))
                    {
                        var elementName = element.data('composite-name');

                        if (params[elementName] != undefined)
                        {
                            params[elementName] = params[elementName] + this.getElementValue(element, false) + ',';

                        }
                        else
                        {
                            params[elementName] = this.getElementValue(element, false) + ",";
                        }
                    }
                    else
                    {
                        elementName = element.attr('name');

                        if(elementType == "checkbox")
                        {
                            if (elementName != undefined) //ignore the checkbox who does not have any name....
                            {
                                if(element.prop('checked'))
                                {
                                    params[elementName] = "true";
                                }
                                else
                                {
                                    params[elementName] = "false";
                                }
                            }
                        }
                        else if(elementType == "radio")
                        {
                            params[elementName] = this.getElementValue(element, false);
                        }
                    }
                }
            }
        }
        return params;
    },

    appendFormElements: function (parent, html)
    {
        $("#" + parent).html(html);
    },

    getElementValue: function (element,doCharEscaping)
    {
        if (doCharEscaping)
        {
            return formManager.replaceSqlEscapeChar(element.val());
        }

        else
        {
            return element.val();
        }
    },

    replaceSqlEscapeChar: function (content)
    {
        if (content !== undefined)
        {
            return content.replace(/[']/g, '\'$&');
        }
    },

    // ------------------------------------------------------------------------Render form data--------------------------------------------------------------------------------------------------//

    renderForm: function (context)
    {
        var elements, elementSize, element;

        var elementName;

        var elementType;

        var elementValue;

        var multipleSelectType;

        if (context.json != undefined && context.json.data != undefined)
        {
            if (context.resultIndex)
            {
                result = context.json.data;
            }

            else
            {
                result = context.json.data;
            }

            elements = context.container.find('input[type=text],textarea,input[type=password],select,input[type=email],input[type=checkbox],input[type=radio]');

            if (elements != undefined && elements.length > 0)
            {
                elementSize = elements.length;

                for (var elementIndex = 0; elementIndex < elementSize; elementIndex++)
                {
                    element = $(elements[elementIndex]);

                    if(context.renderByJsonKey)
                    {
                        if(element.data('json-key') != undefined)
                        {
                            elementName = element.data('json-key');
                        }
                        else
                        {
                            elementName = element.attr('name');
                        }
                    }
                    else
                    {
                        elementName = element.attr('name');
                    }

                    elementType = element.prop('type');

                    elementValue = element.data('value');

                    multipleSelectType = element.attr('data-type');

                    if (elementType == 'text' || elementType == 'textarea' || elementType == 'password'|| elementType == 'email')
                    {
                        element.val(result[elementName]);
                    }
                    else if (elementType == 'checkbox')
                    {
                        element.prop('checked', false);

                        if(elementValue != undefined)
                        {
                            if(result[elementName] && elementValue == "false")
                            {
                                element.prop('checked', false);
                            }
                            else if(elementValue == "true")
                            {
                                element.prop('checked', true);
                            }
                        }
                        else if(result[elementName] === "false")
                        {
                            element.prop('checked', false);
                        }
                        else if(result[elementName])
                        {
                            element.prop('checked', true);
                        }
                    }

                    else if (elementType == 'radio')
                    {
                        element.prop('checked', false);

                        if (element.val() == result[elementName])
                        {
                            element.prop('checked', true);
                        }

                        // for global-settings
                        if(result[0] != undefined)
                        {
                            if(element.val() == result[0][elementName])
                            {
                                element.prop('checked', true);
                            }
                        }
                    }

                    else if(elementType == 'select-one')
                    {
                        formManager.convertToDropDown(element,result[elementName]);
                    }

                    else if(multipleSelectType && multipleSelectType == 'multiple')
                    {
                        formManager.convertToMultiSelectDropDown(element,result[elementName]);
                    }
                }
            }

            if (context.compositeElement)
            {
                element = $("#" + context.compositeElement);

                var compositeValues = result[element.attr('name')];

                if (compositeValues != undefined)
                {
                    compositeValues = compositeValues.split(',');

                    for (var compositeValueIndex = 0; compositeValueIndex < compositeValues.length; compositeValueIndex++)
                    {
                        if (compositeValues[compositeValueIndex])
                        {
                            $('input[value=' + compositeValues[compositeValueIndex] + ']').prop("checked", true);

                        }
                    }
                }
            }
        }

        if (context.postCallback != undefined)
        {
            var callbacks = $.Callbacks();

            callbacks.add(context.postCallback);

            callbacks.fire(context);

            callbacks.remove(context.postCallback);
        }

        loaderUtil.hideModalLoader();

        loaderUtil.hideCentralModalLoader();
    },

    // -------------------------------------------------------------------------Render dropdown data-------------------------------------------------------------------------------------------------//

    convertToDropDown:function (element,roleId)
    {
        if(roleId)
        {
            var elementId = element[0].getAttribute('id');

            var dropdownlist = $("#"+elementId).data('kendoDropDownList');

            dropdownlist.value(roleId);
        }
    },

    // ---------------------------------------------------------------------------Render multiselect data-----------------------------------------------------------------------------------------------//

    convertToMultiSelectDropDown : function (element,roleId)
    {
        if(roleId)
        {
            var value = roleId.split(",");

            var elementId = element[0].getAttribute('id');

            var dropdownlist = $("#"+elementId).data('kendoMultiSelect');

            dropdownlist.value(value);
        }
    },

    // ------------------------------------------------------------------------------Disable fields--------------------------------------------------------------------------------------------//

    disableElements: function (context, elements)
    {
        var query = '';

        for (var element = 0; element < elements.length; element++)
        {

            query += '[name=' + elements[element] + ']' + ",";


        }
        context.container.find(query.substring(0, query.length - 1)).prop("disabled", true);
    },

    // -------------------------------------------------------------------------------Set & get element value for local storage-------------------------------------------------------------------------------------------//

    setElementValue: function (form, elementName, elementValue)
    {
        form.find('[name=' + elementName + ']').val(elementValue);
    },

    getElementByName: function (form, elementName)
    {
        return form.find('[name=' + elementName + ']');
    }
};