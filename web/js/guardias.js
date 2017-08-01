function _JoinServicio(Servicio) {

	var obj = {};

	// $('#fmedico').validator();
	obj['serviceid'] = Servicio;

	$.ajax({
		type : "POST",
		url : _REQUEST_CONTEXT + 'servicio/unirse.jsp',
		data : obj,
		async : false,
		success : function(data) {
			$("#success").modal("show");
			// $(".yainscrito").show();

		},
		error : function(data) {
			console.log("error");
		}
	});

}

function _changeServicio(Servicio, NeedRefresh) {

	var obj = {};

	// $('#fmedico').validator();
	obj['serviceid'] = Servicio;

	$.ajax({
		type : "POST",
		url : _REQUEST_CONTEXT + 'medico/cambiar_sesion.jsp',
		data : obj,
		async : false,
		success : function(data) {
			createCookie("ServicioId", Servicio + "|" + _USER_LOGGED, 30);
			if (NeedRefresh)
				window.location.reload();
		},
		error : function(data) {
			console.log("error");
		}
	});

}

function EditarMedico(ID) {

	$("#editarmedico").load("medico/detallemedico.jsp?q=12123&id=" + ID);

	$("#editarmedico").dialog({
		title : 'Datos del médico',
		position : {
			my : 'top',
			at : 'top+75'
		},
		width : '30%',
		close : function(event, ui) {
			// $("#editarmedico").dialog("destroy");

		}
	});

}

function sortMe(a, b) {
	return a.className < b.className;
}

/* class=orden1 orden2 DE LOS TIPOS DE GUARDIAS */
function _OrdenarGuardias() {

	$('.fc-title').each(function(index) {
		// obtenemos el #residente #tipo A / B
		var elem = $(this).find("div").sort(sortMe);

		$(this).append(elem);
	})

}

function createCookie(name, value, days) {
	var expires = "";
	if (days) {
		var date = new Date();
		date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
		expires = "; expires=" + date.toUTCString();
	}
	document.cookie = name + "=" + value + expires + "; path=/";
}

function _IsMedicoONCall(MedicoId, Event) {
	bExiste = false;

	// verificamos si el evento contiene el medico =13"
	if (Event.indexOf("id=" + MedicoId.replace('"', "") + ">") >= 0)
		bExiste = true;

	return bExiste;

}

function _FillDataDays() {
	// recorremos la tabla de datos de medicos y buscamos en el grid de
	// calendario

	// quitamos opcion de excel

	if ($("#calendar div.adjunto").length > 0) {
		$("#toExcel").show();
		$("#tour").show();
	} else {
		$("#toExcel").hide();
		$("#tour").hide();
	}

	$("#doctor_list table tr")
			.each(
					function(index) {

						Guardias = $("div." + _ADJUNTO + "[id="
								+ $(this).attr("id") + "]").length;
						var ID_INICIAL = $(this).attr("id");
						// console.log("Id Medico:" + ID_INICIAL);
						// console.log("ADJUNTO:" + _ADJUNTO);
						Localizada = 0;
						Presencia = 0;
						Refuerzo = 0;
						LocalizadaF = 0;
						PresenciaF = 0;
						RefuerzoF = 0;
						Simulados = 0;
						ID = -1;
						// recorremos todos los dias
						// console.log(".fc-title div."+ _ADJUNTO + "[id=" +
						// ID_INICIAL +"]");

						$(
								".fc-title div." + _ADJUNTO + "[id="
										+ ID_INICIAL + "]")
								.each(
										function(index) {

											/*
											 * ESTO ES PARA EVITAR QUE NO ME
											 * CONTABILICE LOS DIAS DE OTROS
											 * MESES
											 */
											tdEVENT = $(this).closest("td");
											diaMES = $(this).closest("td")
													.closest("table").find(
															"thead").find("td")
													.eq(tdEVENT.index());

											// console.log(tdEVENT.html() +
											// ",fc-other-month:" +
											// diaMES.hasClass("fc-other-month"));

											if (diaMES
													.hasClass("fc-other-month"))
												return;
											/*
											 * FIN ESTO ES PARA EVITAR QUE NO ME
											 * CONTABILICE LOS DIAS DE OTROS
											 * MESES
											 */

											var bEsPresencia = false;
											if ($(this).hasClass(_REFUERZO)) {
												if ($(this)
														.hasClass("festivoc"))
													RefuerzoF += 1;
												else
													Refuerzo += 1;
											}
											if ($(this).hasClass(_LOCALIZADA)) {
												if ($(this)
														.hasClass("festivoc"))
													LocalizadaF += 1;
												else
													Localizada += 1;
											}

											if ($(this).hasClass(_PRESENCIA)) {
												if ($(this)
														.hasClass("festivoc"))
													PresenciaF += 1;
												else
													Presencia += 1;

												bEsPresencia = true; // nos
																		// vvale
																		// para
																		// no
																		// computar
																		// refuerzos
																		// ni
																		// localizadas
																		// en
																		// los
																		// simulados
											}
											// simulado es si contiene el class
											// simulado en alguno de los
											// residentes asociados
											// class="orden3 adjunto presencia
											// nosimulado
											// buscamos en el parent
											// orden1 residente simulado
											if (bEsPresencia) {
												$(this)
														.parent()
														.find("div")
														.each(
																function(index) {

																	if ($(this)
																			.hasClass(
																					_SIMULADO
																							.toLowerCase()))
																		Simulados += 1;
																});
											}

										});
						if (ID_INICIAL != -1) {
							$(
									"#doctor_list table tr[id=" + ID_INICIAL
											+ "] td.mespresencia").html(
									"<span class=\"badge\">" + Presencia
											+ "</span>");
							$(
									"#doctor_list table tr[id=" + ID_INICIAL
											+ "] td.meslocalizada").html(
									"<span class=\"badge\">" + Localizada
											+ "</span>");
							$(
									"#doctor_list table tr[id=" + ID_INICIAL
											+ "] td.mesrefuerzo").html(
									"<span class=\"badge\">" + Refuerzo
											+ "</span>");
							$(
									"#doctor_list table tr[id=" + ID_INICIAL
											+ "] td.mespresenciaf").html(
									"<span class=\"badge\">" + PresenciaF
											+ "</span>");
							$(
									"#doctor_list table tr[id=" + ID_INICIAL
											+ "] td.meslocalizadaf").html(
									"<span class=\"badge\">" + LocalizadaF
											+ "</span>");
							$(
									"#doctor_list table tr[id=" + ID_INICIAL
											+ "] td.mesrefuerzof").html(
									"<span class=\"badge\">" + RefuerzoF
											+ "</span>");
							$(
									"#doctor_list table tr[id=" + ID_INICIAL
											+ "] td.mestotaladjunto").html(
									"<span class=\"badge\">"
											+ (Presencia + Localizada
													+ Refuerzo + PresenciaF
													+ LocalizadaF + RefuerzoF)
											+ "</span>");
							$(
									"#doctor_list table tr[id=" + ID_INICIAL
											+ "] td.messimulados_adjunto")
									.html(
											"<span class=\"badge\">"
													+ Simulados + "</span>");

							// alert($(ID_INICIAL + "_data").html());
							Localizada = 0;
							Presencia = 0;
							Refuerzo = 0;
							LocalizadaF = 0;
							PresenciaF = 0;
							RefuerzoF = 0;
						}
						// RESIDENTES
						Guardias = $("div." + _RESIDENTE + "[id="
								+ $(this).attr("id") + "]").length;
						FestivosR = 0;
						// recorremos todos los dias
						$(
								".fc-title div." + _RESIDENTE + "[id="
										+ ID_INICIAL + "]").each(
								function(index) {

									/*
									 * ESTO ES PARA EVITAR QUE NO ME CONTABILICE
									 * LOS DIAS DE OTROS MESES
									 */
									tdEVENT = $(this).closest("td");
									diaMES = $(this).closest("td").closest(
											"table").find("thead").find("td")
											.eq(tdEVENT.index());
									if (diaMES.hasClass("fc-other-month"))
										return;

									if ($(this).hasClass("festivoc"))
										FestivosR += 1;

								});

						var _totalDiario = Guardias - FestivosR;

						$(
								"#doctor_list table tr[id=" + ID_INICIAL
										+ "] td.mestotalresidente")
								.html(
										"<span class=\"badge\">" + Guardias
												+ "</span>");
						$(
								"#doctor_list table tr[id=" + ID_INICIAL
										+ "] td.mesfestivos").html(
								"<span class=\"badge\">" + FestivosR
										+ "</span>");
						$(
								"#doctor_list table tr[id=" + ID_INICIAL
										+ "] td.mesdiario").html(
								"<span class=\"badge\">" + _totalDiario
										+ "</span>");

					});

}

function fSaveDatabaseOrExcel(Operation) {

	var _page = "medico/grabar_guardias.jsp";

	if (Operation == 'excel' || Operation == 'excelmail')
		_page = "toExcel.jsp";

	var aGuardias = [];

	// var _Date = $('#calendar').fullCalendar('getDate').format("YYYY-MM-DD");

	var LastIDADJUNTOPRESENCIA = 0; // para saber el ultimo y reordenar la
									// secuecia

	/* CADA DIA */
	var contador = 0;
	var _Date;

	var _Date = $('#calendar').fullCalendar('getDate');

	var dayWrapper = moment(_Date);
	/* METEMOS EL DIA A 1 */
	dayWrapper.date(1);

	$('.fc-title').each(function(index) {

		fguardia = dayWrapper.format("YYYY-MM-DD");

		$(this).find("div").each(function(index2) {

			var _oGuardia = {};

			_oGuardia["DiaGuardia"] = fguardia;

			_oGuardia["IdMedico"] = $(this).attr("id");
			_oGuardia[_ADJUNTO] = 0;

			/* ultimo adjunto de presencia */
			if ($(this).hasClass(_ADJUNTO) && $(this).hasClass(_PRESENCIA))
				LastIDADJUNTOPRESENCIA = $(this).attr("id");
			if ($(this).hasClass(_ADJUNTO)) {
				_oGuardia[_ADJUNTO] = 1;
			}
			_oGuardia["Festivo"] = 0;
			if ($(this).hasClass("festivoc")) {
				_oGuardia["Festivo"] = 1;
			}
			_oGuardia["Tipo"] = _PRESENCIA;
			if ($(this).hasClass(_RESIDENTE)) {
				_oGuardia["Tipo"] = "";
			}
			if ($(this).hasClass(_LOCALIZADA)) {
				_oGuardia["Tipo"] = _LOCALIZADA;
			}
			if ($(this).hasClass(_REFUERZO)) {
				_oGuardia["Tipo"] = _REFUERZO;
			}

			aGuardias[contador] = _oGuardia;
			contador++;

		});

		dayWrapper.add(1, "d");

	});

	var _urlPage = _REQUEST_CONTEXT + _page;

	// por ajax parece que no me lo fuerza la descarga

	_paramExcelMail = "0";
	if (Operation == 'excel') {
		$("#guardias").val(JSON.stringify(aGuardias));
		var _Date1 = $('#calendar').fullCalendar('getDate').date(1).format(
				"YYYY-MM-DD");
		$("#MesGuardia").val(_Date1);
		$("#ff3").submit();

	} else {

		var _IdLoadiongDiv = "loading";
		_paramExcelMail = "1";
		if (Operation == 'excelmail')
			_IdLoadiongDiv = "loadingmail";

		$("#" + _IdLoadiongDiv).modal("show");
		var _Date1 = $('#calendar').fullCalendar('getDate').date(1).format(
				"YYYY-MM-DD");
		$
				.ajax({
					data : {
						guardias : JSON.stringify(aGuardias),
						MesGuardia : _Date1,
						ByEmail : _paramExcelMail
					}, // stringify is important,
					type : 'POST',
					dataType : 'JSON',
					url : _urlPage,
					complete : function(data) {
						if (data.responseText.indexOf("[Error]") > 0) {
							$("#" + _IdLoadiongDiv).modal("hide");
							$('#loaded #loadedtitle').html("Incorrecto");
							$('#loaded #loadedbody').html(data.responseText);
							$('#loaded').modal("show");
						} else {
							if (data.responseText.indexOf("OK") >= 0) {

								$("#" + _IdLoadiongDiv).modal("hide");
								$('#loaded #loadedtitle').html("Correcto");

								if (Operation != 'excelmail') {
									$('#loaded #loadedbody')
											.html(
													"Los datos han sido almacenados correctamente");
									_fn_OrdenarSecuenciaAdjuntos(LastIDADJUNTOPRESENCIA);
								} else
									$('#loaded').modal('show');
							} else
								// auth issue
								window.location.href = _REQUEST_CONTEXT
										+ 'login.jsp';

						}

					},
					fail : function(data) {
						alert("error");
						/*
						 * $('#loading').modal("hide"); $('#loaded
						 * #loadedtitle').html("Error"); $('#loaded
						 * #loadedbody').html(data.responseText);
						 * $('#loaded').modal('show');
						 */
					}
				});

	} // fin de ajax call y frame target

}

var _DateClicked = "";
var _EventToChange;

/* hay que ordenar para que aparezca en el orden correcto */
function _fn_OrdenarSecuenciaAdjuntos(IDMEDICO) {
	$.ajax({
		data : {
			lastidmedico : IDMEDICO
		}, // stringify is important,
		type : 'POST',
		url : _REQUEST_CONTEXT + 'medico/reordenar_grabar_guardias.jsp',
		complete : function(data) {
			$('#loading').modal('hide');
			$('#loaded').modal('show');
		},
		fail : function(data) {

			alert("Error" + data);
		}
	});

}
/*
 * <li><a onclick =_state('<%=Util.eEstadoCambiosGuardias.APROBADA%>',<%=oCambio.getIdCambio()%>)
 * href="#">SI,CESION</a></li> <li><a onclick =_state('<%=Util.eEstadoCambiosGuardias.APROBADA%>',<%=oCambio.getIdCambio()%>)
 * href="#">SI,INTERCAMBIO</a></li> <li><a onclick =_state('<%=Util.eEstadoCambiosGuardias.CANCELADA%>',<%=oCambio.getIdCambio()%>)
 * href="#">NO</a></li> <li><a onclick =_state('<%=Util.eEstadoCambiosGuardias.RECHAZADA%>',<%=oCambio.getIdCambio()%>)
 * href="#">CANCELAR</a></li>
 */

function _addChange(ChangeData) {

	$.ajax({
		data : {
			datachange : ChangeData
		},
		type : 'POST',
		url : _REQUEST_CONTEXT + 'medico/add_cambio_guardia.jsp',
		dataType : 'JSON',
		complete : function(data) {
			if (data.responseText.indexOf("NOOK") >= 0) {
				$("#error .panel-body").html(data.responseText);
				$("#error").show();
			} else // OJO, SI VIENE CON ADMIN, LLAMAMOS A CAMBIAR ESTADO EN UN
					// UNICO PASO
			{
				if (_IS_A) // admin, cogwemos el ID devuelto por el add guardia
				{
					// vmatch = /[0-9]+/.exec(data.responseText).toString();
					var JSONADDCambioData = JSON.parse(data.responseText);
					// aprobamos
					_changeState(JSONADDCambioData.Estado,
							JSONADDCambioData.IdCambio,
							JSONADDCambioData.IdMedicoDestino,
							JSONADDCambioData.TipoCambio);

				} else {
					$("#success").show();
					$('#enviar').prop('disabled', true);
				}
			}

		},
		fail : function(data) {
			$("#error").show();
		}
	});
}

function _newChange(idmedico, inicio, fin) {

	$("#confirmar_cambio").load(
			"medico/_list_medicos_cambio_guardia.jsp?idsolicitante=" + idmedico
					+ "&inicio=" + inicio + "&fin=" + fin);

	$("#confirmar_cambio").dialog({
		title : 'Datos del cambio',
		position : {
			my : 'top',
			at : 'top+75'
		},
		width : '30%',
		close : function(event, ui) {
			/*
			 * REFRESCAMOS SI ES VISIBLE window.location.refresh();
			 */

		}
	});
}

function _state(newstate, idchange, type, destinatario) {

	if (newstate != _CANCELADA) {

		$("#confirmar_cambio").load(
				"medico/_list_medicos_cambio_guardia.jsp?cambioid=" + idchange
						+ '&newstate=' + newstate + '&type=' + type);

		$("#confirmar_cambio").dialog({
			title : 'Datos del cambio',
			position : {
				my : 'top',
				at : 'top+75'
			},
			width : '30%',
			close : function(event, ui) {
				// $("#editarmedico").dialog("destroy");

			}
		});
	} else
		// CANCELADA, no MUESTRO FORMULARIO
		_changeState(_CANCELADA, idchange, destinatario, type);

}

function _changeState(newstate, idchange, MedicoDestination, Type) {

	$.ajax({
		data : {
			id : idchange,
			state : newstate,
			MedicoDestination : MedicoDestination,
			type : Type
		},
		type : 'POST',
		url : _REQUEST_CONTEXT + 'medico/cambio_estado_guardia.jsp',
		complete : function(data) {
			/* EL DATA DEVUELVO EL NOMBRE Y APELLIDOS DEL MEDICO */
			$('#' + idchange + '_destinatario').html(data);
			$('#' + idchange + '_tipocambio').html(Type);
			$('#' + idchange + '_aprobacion').html(
					moment().format("YYYY-MM-DD"));
			$('#' + idchange + "_estado").html(newstate);
			if (_APROBADA == newstate)
				$('#' + idchange + "_action").html('');

			$("#success").show();

		},
		fail : function(data) {
			$("#error").show();
		}
	});

}

function fn_Save() {

	var _myindex = 1;

	var obj = {};

	$('<input>').attr({
		type : 'hidden',
		id : 'start_calc',
		name : 'start_calc',
		value : $('#calendar').fullCalendar('getDate').format("YYYY-MM-DD")
	}).appendTo('#fSave');

	/*
	 * obj["start"] =
	 * $('#calendar').fullCalendar('getDate').format("YYYY-MM-DD"); obj["end"] =
	 * $('#calendar').fullCalendar('getDate').format("YYYY-MM-DD");
	 */
	$('<input>').attr({
		type : 'hidden',
		id : 'end_calc',
		name : 'end_calc',
		value : $('#calendar').fullCalendar('getDate').format("YYYY-MM-DD")
	}).appendTo('#fSave');

	$('.fc-title').each(function(index) {
		// obtenemos el #residente #tipo A / B
		var _Tipo = $(this).find("#poolday #pool:checked");
		// alert(_Tipo);
		// obtenemos si es festivo
		var _Festivo = $(this).find(".festivo input");

		// console.log( $(this).find(".festivo input:checked"));

		$('<input>').attr({
			type : 'hidden',
			name : 'poolday' + _myindex,
			value : _Tipo.val()

		}).appendTo('#fSave');

		// obj["tipo" + _myindex] = _Tipo.val();

		// _GET_DATA += ",tipo" + _myindex + "=" _Tipo.val();
		var _festivoValue = false;
		if ($(_Festivo).is(':checked')) {
			// if (_Festivo.val()=="V")
			_festivoValue = true;
		}

		$('<input>').attr({
			type : 'hidden',
			name : 'festivo' + _myindex,
			value : _festivoValue

		}).appendTo('#fSave');

		// obj["festivo" + _myindex] = _festivoValue;
		_myindex++;
		// console.log( index + ": " + $( this ).text() );
	});

	$("#fSave").submit();

}

function fn_FillMonth(bIsCalculating) {

	// todos seleccionados
	// todos seleccionados

	/* hay datos previos, avisamos y submit */
	if (bIsCalculating) {
		$(function() {
			$("#dialog-confirm").dialog(
					{
						resizable : false,
						height : "auto",
						// width: 400,
						modal : true,
						buttons : {
							"Continuar" : function() {
								$(this).dialog("close");
								var _pageTo = _REQUEST_URI;
								$("#fecha").val(
										$('#calendar').fullCalendar('getDate')
												.format("YYYY-MM-DD"));
								// alert($("#fecha").val());
								$("#ff").submit();
							},
							"Cancelar" : function() {
								$(this).dialog("close");
							}
						}
					});
			var buttons = $('.ui-dialog-buttonpane div').children('button');
			buttons.removeClass().addClass('btn btn-block btn-primary');
			var buttonsCAPA = $('.ui-dialog-buttonset').removeClass();
		});

	} else // normal
	{
		var _pageTo = _REQUEST_URI;
		$("#fecha").val(
				$('#calendar').fullCalendar('getDate').format("YYYY-MM-DD"));
		// alert($("#fecha").val());
		$("#ff").submit();
	}

}