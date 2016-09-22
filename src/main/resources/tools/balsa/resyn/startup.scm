;;;
;;;	The Balsa Asynchronous Hardware Synthesis System
;;;	Copyright (C) 1995-2003 Department of Computer Science
;;;	The University of Manchester, Oxford Road, Manchester, UK, M13 9PL
;;;	
;;;	This program is free software; you can redistribute it and/or modify
;;;	it under the terms of the GNU General Public License as published by
;;;	the Free Software Foundation; either version 2 of the License, or
;;;	(at your option) any later version.
;;;	
;;;	This program is distributed in the hope that it will be useful,
;;;	but WITHOUT ANY WARRANTY; without even the implied warranty of
;;;	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;;	GNU General Public License for more details.
;;;	
;;;	You should have received a copy of the GNU General Public License
;;;	along with this program; if not, write to the Free Software
;;;	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
;;;
;;;	`startup.scm'
;;; resyn style startup
;;;

(set! tech-sync-channel-portions
	`((req push ,width-one) (ack pull ,width-one)))

(set! tech-push-channel-portions 
	`((req push ,width-one) (ack pull ,width-one) (data push ,width-n)))

(set! tech-pull-channel-portions 
	`((req push ,width-one) (ack pull ,width-one) (data pull ,width-n)))

(for-each (lambda (component)
   	(if (list? component) ; (name from-name from-style)
		(brz-add-primitive-part-implementation (car component)
			`(style "resyn" (include style ,(caddr component) ,(cadr component)))
		)
		(brz-add-primitive-part-implementation component
			`(style "resyn" (include style "resyn" ,component))
		)
    )
) '(
    "Adapt"
    "Arbiter"
    "BinaryFunc"
    "BinaryFuncConstR"
    "CallMux"
    "Case"
    "CaseFetch"
    "Combine"
    "CombineEqual"
    "Constant"
    "Encode"
    "FalseVariable"
    "Fetch"
    "Variable"
    "While"
))
