(function(c) {
	//Si jQuery no está definido no se puede continuar
	if (!jQuery) {
		console.error("jQuery no está cargado");
		return;
	}
	
	var domainURL = location.protocol+'//'+ location.hostname;
	
	var TWITTER_KEY = "twitter";
	var FACEBOOK_KEY = "facebook";
	var LINKEDIN_KEY = "linkedin";
	var WHATSAPP_KEY = "whatsapp";
	
	function SocialNetwork(){};
	SocialNetwork.prototype.setUpShare = function(params) {
		var shareText = params.jqNode.find(params.titleClass).text();
		var shareURL = params.jqNode.find(params.anchorClass).attr("href");
		var snListNode = params.jqNode.closest(params.parentClass).find("#redes ul");
		
		if(typeof shareText != "undefined" && typeof shareURL != "undefined" && snListNode.length > 0)
			snListNode.append('<li><a href="'+this.getPreparedSharedURL(shareURL, shareText)+'" data-title="'+shareText+'"data-rrss="'+this.key+'" rel="pop-up"><img src="' + this.shareImg + '" title="' + this.key + '" width="32" height="32"/></a></li>')
	}
	SocialNetwork.prototype.domainSafeURL = function (url) {
		if (url.match(/^(https?:\/\/)([0-9A-Za-z]+\.)+[A-Za-z]{2,}\/?/gi) == null)
			return domainURL + url;
		else
			return url;
	}
	
	//Twitter
	function TwitterSN(urlToShare, text){SocialNetwork.call(this, urlToShare, text);};
	TwitterSN.prototype = Object.create(SocialNetwork.prototype);
	TwitterSN.prototype.key = TWITTER_KEY;
	TwitterSN.prototype.shareImg = "/Guardias/img/rrss/twitter_blanco.png";
	TwitterSN.prototype.getPreparedSharedURL = function (url, text) {
		return 'http://twitter.com/share?text='+text+'&url='+ this.domainSafeURL(url);
	}
	
	//Facebook
	function FacebookSN(urlToShare, text){SocialNetwork.call(this, urlToShare, text);};
	FacebookSN.prototype = Object.create(SocialNetwork.prototype);
	FacebookSN.prototype.key = FACEBOOK_KEY;
	FacebookSN.prototype.shareImg = "/Guardias/img/rrss/facebook_blanco.png";
	FacebookSN.prototype.getPreparedSharedURL = function (url, text) {
		return 'https://facebook.com/dialog/share?app_id=1722618318038561&display=popup&href='+ this.domainSafeURL(url) + "&redirect_uri=" + location.href;
	}
	
	//LinkedIn
	function LinkedInSN(urlToShare, text){SocialNetwork.call(this, urlToShare, text);};
	LinkedInSN.prototype = Object.create(SocialNetwork.prototype);
	LinkedInSN.prototype.key = LINKEDIN_KEY;
	LinkedInSN.prototype.shareImg = "/Guardias/img/rrss/linkedin_blanco.png";
	LinkedInSN.prototype.getPreparedSharedURL = function (url, text) {
		return 'https://www.linkedin.com/shareArticle?url=' + this.domainSafeURL(url);
	}
	
	//WhatsApp
	function WhatsAppSN(urlToShare, text){SocialNetwork.call(this, urlToShare, text);};
	WhatsAppSN.prototype = Object.create(SocialNetwork.prototype);
	WhatsAppSN.prototype.key = WHATSAPP_KEY;
	WhatsAppSN.prototype.shareImg ="/Guardias/img/rrss/whatsapp.png";
	WhatsAppSN.prototype.getPreparedSharedURL = function (url, text) {
		return 'whatsapp://send?text='+ encodeURI(text) + '%0A%0A' + this.domainSafeURL(url);
	}
	
	//Este array contiene los indicadores de las redes sociales activas
	var enabledSocialNetworks = [TWITTER_KEY, FACEBOOK_KEY, LINKEDIN_KEY, WHATSAPP_KEY];
		
	function SNSharerFactory() {
		this.create = function(key) {
			var snSharer;
			if (key == TWITTER_KEY) {
				snSharer = new TwitterSN();
			} else if (key == FACEBOOK_KEY) {
				snSharer = new FacebookSN();
			} else if (key == LINKEDIN_KEY) {
				snSharer = new LinkedInSN();
			} else if (key == WHATSAPP_KEY) {
				snSharer = new WhatsAppSN();
			}
			return snSharer;
		};
	};
	
	c.mostrarRRSS = c.mostrarRRSS || function(parentClass, anchorClass, titleClass) {
		
		jQuery(function(){
			var socialNetworkSharers = [];
			var snSharerFactory = new SNSharerFactory();
			for (var i = 0; i < enabledSocialNetworks.length; i++) {
				socialNetworkSharers.push(snSharerFactory.create(enabledSocialNetworks[i]));
			}
			
			$("."+ parentClass).each(function(index){
				try {
					var element = jQuery(this);
					
					if (element.data("shared"))
						return;
					
					for (var i = 0; i < socialNetworkSharers.length; i++) {
						socialNetworkSharers[i].setUpShare({
							"jqNode": element,
							"parentClass": "." + parentClass,
							"anchorClass": "." + anchorClass,
							"titleClass": "." + titleClass
						});
					}
					element.data("shared", true);
				} catch (error) {
					console.error("No se ha podido compartir el elemento:");
					console.dir(this);
				}
			});
			
			//Se asignan eventos para Google analytics
			$("."+ parentClass + ' .redes_sociales a').on('click', function() {
				if (typeof _gaq !== 'undefined') {
					_gaq.push(['_trackEvent', $(this).attr("data-rrss"),'click', $(this).attr("data-title")]);
				}
			});
		});
	};
})(window);