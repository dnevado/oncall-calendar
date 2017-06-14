		<div id="step30" style="display:none; position: absolute;left: 50%;top: 50%;transform: translate(-50%,-50%);"  data-step='30'  data-intro='Por último, selecciona el tipo de cambio y el destinatario consensuado previamente'>
			<img src="<%=request.getContextPath()%>/tour/tour_step3.png"/>
		</div>
    <script type="text/javascript">
      function startIntro(){
    	
    	$("#step30").insertBefore("#calendar")
    	  
        var intro = introJs();
        $("#step30").show();
          //intro.start();
        intro.onchange(function(targetElement) {
        	  
        	//alert(targetElement.id);
        	 if (targetElement.id!=="step30")   // id de la imagen         		
        		 $("#step30").hide();
        	 else
        		 $("#step30").show(); 		
        	 
        	 
		});
          intro.onexit(function() {
        	  $("#step30").hide();
		  });
          intro.start();
      }
    </script>